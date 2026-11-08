package org.pixel.graphics.opengl.render;

import org.pixel.commons.lifecycle.State;
import org.pixel.content.Texture;
import org.pixel.content.opengl.GLTexture;
import org.pixel.graphics.RenderStats;
import org.pixel.graphics.backend.BackendCapabilities;
import org.pixel.graphics.backend.BatchList;
import org.pixel.graphics.backend.MaterialProgram;
import org.pixel.graphics.backend.PassState;
import org.pixel.graphics.backend.PostParams;
import org.pixel.graphics.backend.PostProgram;
import org.pixel.graphics.backend.RenderTarget;
import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.backend.RenderData;
import org.pixel.graphics.backend.VertexLayout;
import org.pixel.graphics.backend.VertexStream;
import org.pixel.graphics.material.Material;
import org.pixel.graphics.opengl.GLApi;
import org.pixel.graphics.opengl.GLProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import static org.pixel.graphics.opengl.GLApi.*;

/**
 * OpenGL 3.3 / OpenGL ES 3.0 implementation of the URP v2 {@link RenderBackend}, shared by every platform through
 * {@link GLApi}.
 * <p>
 * Each {@link VertexLayout} has its own program, VAO and vertex buffer. Quads are drawn indexed from one static
 * index buffer ({@code 0,1,2, 2,3,0} per quad, 16-bit indices). Vertices are uploaded in chunks of at most
 * {@link #MAX_QUADS_PER_BATCH} quads per layout (one upload per layout in the common case) and each batch is drawn
 * with an index offset into its chunk, so no base-vertex draw call is needed (GLES 3.0 compatible). Shape batches
 * bind their slice of the pass's clip and style tables as uniform buffer ranges. Batches with a material use the
 * material's program (compiled by the material through {@link #compileMaterial}) with the shape layout's buffers.
 * <p>
 * Owned and disposed by the graphics device; requires a current GL context.
 */
public final class GLRenderBackend implements RenderBackend {

    /**
     * 16384 quads = 65536 vertices, the most a 16-bit index can address.
     */
    public static final int MAX_QUADS_PER_BATCH = 16384;
    private static final int MAX_TEXTURE_SLOTS = 16;
    private static final int MAX_CLIPS = 256;   // 4 KB uniform block
    private static final int MAX_STYLES = 128;  // 16 KB uniform block (8 vec4 per style; GLES 3.0 guarantees 16 KB)

    private final GLApi gl;
    private final RenderStats stats;
    private final BackendCapabilities capabilities;
    // GL objects: created with the context, and again by contextRecreated()
    private LayoutPipeline spritePipeline;
    private LayoutPipeline shapePipeline;
    private int ebo;
    private int clipUbo;
    private int styleUbo;
    private int emptyVao; // bound for attribute-less full-screen draws
    private final int clipBlockBytes = MAX_CLIPS * BatchList.CLIP_FLOATS * Float.BYTES;
    private final int styleBlockBytes = MAX_STYLES * BatchList.STYLE_FLOATS * Float.BYTES;
    private int whiteTexture;
    private final int[] boundTextures;
    private final FloatBuffer matrixBuffer = directFloats(16);
    private FloatBuffer tableUpload = directFloats(1024); // staging for clip/style tables

    private State state = State.INITIALIZED;
    private boolean inPass;
    private LayoutPipeline current;
    private ProgramState currentProgram;
    private int passSerial;
    private GLRenderTarget passTarget;           // target of the current pass (null = screen)
    private final int[] savedViewport = new int[4];
    private final int[] viewport = new int[4];
    private int savedFramebuffer;
    private int generation; // bumped when the context is recreated

    /**
     * Constructor (requires a current OpenGL 3.3 or OpenGL ES 3.0 context).
     *
     * @param gl    The platform's GL functions.
     * @param stats The device statistics to record GPU work into.
     */
    public GLRenderBackend(GLApi gl, RenderStats stats) {
        if (gl == null || stats == null) {
            throw new IllegalArgumentException("GL functions and render stats cannot be null.");
        }
        this.gl = gl;
        this.stats = stats;

        int slots = Math.max(1, Math.min(gl.getInteger(GL_MAX_TEXTURE_IMAGE_UNITS), MAX_TEXTURE_SLOTS));
        int alignmentBytes = Math.max(16, gl.getInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT));
        int alignmentFloats = (alignmentBytes / Float.BYTES + 3) / 4 * 4; // whole vec4s
        this.capabilities = new BackendCapabilities(slots, MAX_QUADS_PER_BATCH, MAX_CLIPS, MAX_STYLES,
                alignmentFloats);
        this.boundTextures = new int[slots];
        createGlObjects();
    }

    private void createGlObjects() {
        int slots = capabilities.maxTextureSlots();
        whiteTexture = createWhiteTexture();

        ebo = gl.genBuffer();
        spritePipeline = new LayoutPipeline(VertexLayout.SPRITE, new GLSpriteProgram(gl, slots), slots, ebo);
        gl.bufferData(GL_ELEMENT_ARRAY_BUFFER, createQuadIndices(MAX_QUADS_PER_BATCH), GL_STATIC_DRAW);
        configureSpriteAttributes();
        shapePipeline = new LayoutPipeline(VertexLayout.FULL,
                new GLShapeProgram(gl, slots, MAX_CLIPS, MAX_STYLES), slots, ebo);
        configureShapeAttributes();
        gl.bindVertexArray(0);
        gl.bindBuffer(GL_ARRAY_BUFFER, 0);

        clipUbo = gl.genBuffer();
        styleUbo = gl.genBuffer();
        emptyVao = gl.genVertexArray();
    }

    /**
     * The GL context was lost and a new one is current (Android can recreate it when the app is paused): every GL
     * object of the old context is gone, so recreate them. This object, and the batches that use it, stay valid.
     * Textures created in the old context are <b>not</b> restored here.
     */
    public void contextRecreated() {
        checkActive();
        inPass = false;
        current = null;
        currentProgram = null;
        passTarget = null;
        generation++; // targets and effects made before rebuild themselves on next use
        createGlObjects(); // the old names died with their context: nothing to delete
    }

    @Override
    public int getContextGeneration() {
        return generation;
    }

    @Override
    public BackendCapabilities capabilities() {
        return capabilities;
    }

    @Override
    public void beginPass(PassState passState) {
        checkActive();
        if (inPass) {
            throw new IllegalStateException("beginPass() called twice without endPass().");
        }
        RenderTarget target = passState.getTarget();
        passTarget = target != null ? glTarget(target) : null;
        inPass = true;
        if (passTarget != null) {
            bindTarget(passTarget);
            if (passState.isClearTarget()) {
                var c = passTarget.getClearColor(); // stored premultiplied, like everything drawn into targets
                gl.clearColor(c.getRed() * c.getAlpha(), c.getGreen() * c.getAlpha(), c.getBlue() * c.getAlpha(),
                        c.getAlpha());
                gl.clear(GL_COLOR_BUFFER_BIT);
            }
        }

        matrixBuffer.clear();
        passState.getViewMatrix().writeBuffer(matrixBuffer);
        if (passTarget != null) {
            // targets store rows top-down (so their texture samples like an image): flip clip-space y
            for (int column = 0; column < 4; column++) {
                matrixBuffer.put(column * 4 + 1, -matrixBuffer.get(column * 4 + 1));
            }
        }
        passSerial++; // programs upload the matrix when a batch of this pass first uses them
        current = null;
        currentProgram = null;

        // the programs output premultiplied colors, so targets always hold valid premultiplied content
        switch (passState.getBlendMode()) {
            // light: color adds up, coverage doesn't change (premultiplied emission, visible even where alpha is 0)
            case ADDITIVE -> gl.blendFuncSeparate(GL_ONE, GL_ONE, GL_ZERO, GL_ONE);
            case MULTIPLY -> gl.blendFuncSeparate(GL_DST_COLOR, GL_ONE_MINUS_SRC_ALPHA, GL_ZERO, GL_ONE);
            default -> gl.blendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        }

        // Other renderers may have changed texture bindings since the last pass: forget what we think is bound, and
        // make sure every unit the programs sample has a valid texture (some drivers, e.g. macOS, complain otherwise).
        Arrays.fill(boundTextures, -1);
        for (int unit = 0; unit < boundTextures.length; unit++) {
            bindTexture(unit, whiteTexture);
        }
    }

    @Override
    public void submit(RenderData data) {
        checkActive();
        if (!inPass) {
            throw new IllegalStateException("submit() called outside beginPass()/endPass().");
        }
        BatchList batches = data.batches();
        spritePipeline.resetChunk();
        shapePipeline.resetChunk();
        uploadTables(batches);

        for (int b = 0, n = batches.size(); b < n; b++) {
            int first = batches.firstQuad(b);
            int count = batches.quadCount(b);
            if (count == 0) {
                continue;
            }
            if (count > MAX_QUADS_PER_BATCH) {
                throw new IllegalArgumentException("Batch exceeds " + MAX_QUADS_PER_BATCH + " quads.");
            }

            LayoutPipeline pipeline = batches.layout(b) == VertexLayout.SPRITE ? spritePipeline : shapePipeline;
            Material material = batches.material(b);
            ProgramState program = pipeline.program;
            GLMaterialProgram materialProgram = null;
            if (material != null) {
                if (pipeline != shapePipeline) {
                    throw new IllegalArgumentException("Materials apply to shape batches only.");
                }
                materialProgram = materialProgram(material);
                program = materialProgram;
            }
            if (pipeline != current) {
                pipeline.bind();
                current = pipeline;
            }
            if (program != currentProgram) {
                program.program.bind();
                currentProgram = program;
            }
            program.matrix(matrixBuffer, passSerial);
            pipeline.ensureUploaded(data.vertices(batches.layout(b)), first, count);

            int premultiplied = 0;
            for (int slot = 0, t = batches.textureCount(b); slot < t; slot++) {
                Texture texture = batches.texture(b, slot);
                bindTexture(slot, textureId(texture));
                if (texture instanceof GLTexture glTexture && glTexture.isPremultiplied()) {
                    premultiplied |= 1 << slot;
                }
            }
            program.premultiplied(premultiplied);
            if (materialProgram != null) {
                materialProgram.upload(material);
                for (int t = 0, k = material.getTextureCount(); t < k; t++) {
                    bindTexture(materialProgram.firstMaterialUnit + t, textureId(material.getTexture(t)));
                }
            }
            if (pipeline == shapePipeline) {
                gl.bindBufferRange(GL_UNIFORM_BUFFER, GLShapeProgram.CLIP_BINDING, clipUbo,
                        (long) batches.clipOffset(b) * Float.BYTES, clipBlockBytes);
                gl.bindBufferRange(GL_UNIFORM_BUFFER, GLShapeProgram.STYLE_BINDING, styleUbo,
                        (long) batches.styleOffset(b) * Float.BYTES, styleBlockBytes);
            }

            gl.drawElements(GL_TRIANGLES, count * 6, GL_UNSIGNED_SHORT,
                    (long) (first - pipeline.chunkStart) * 6 * Short.BYTES);
            stats.recordDrawCall(count, count * 4L);
        }
    }

    @Override
    public void endPass() {
        if (!inPass) {
            throw new IllegalStateException("endPass() called without beginPass().");
        }
        inPass = false;
        current = null;
        currentProgram = null;

        // restore the shared defaults other renderers rely on
        gl.bindVertexArray(0);
        gl.bindBuffer(GL_ARRAY_BUFFER, 0);
        gl.bindBuffer(GL_UNIFORM_BUFFER, 0);
        gl.activeTexture(GL_TEXTURE0);
        gl.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        gl.useProgram(0);
        if (passTarget != null) {
            restoreFramebuffer();
            passTarget = null;
        }
    }

    // region materials

    @Override
    public MaterialProgram compileMaterial(Material material, String vertexSource, String source) {
        checkActive();
        int slots = capabilities.materialTextureSlots();
        if (slots + Material.MAX_TEXTURES > capabilities.maxTextureSlots()) {
            throw new IllegalStateException("Materials need at least " + (Material.MAX_TEXTURES + 1)
                    + " texture units; this GPU has " + capabilities.maxTextureSlots() + ".");
        }
        GLShapeProgram program = material.getKind() == Material.Kind.EFFECT
                ? GLShapeProgram.effect(gl, slots, MAX_CLIPS, MAX_STYLES, source)
                : GLShapeProgram.raw(gl, slots, vertexSource, source);
        try {
            return new GLMaterialProgram(gl, program, slots, generation);
        } catch (RuntimeException e) {
            program.dispose();
            throw e;
        } finally {
            currentProgram = null; // setting up the program unbound the current one
        }
    }

    private GLMaterialProgram materialProgram(Material material) {
        MaterialProgram program = material.program(this);
        if (!(program instanceof GLMaterialProgram glProgram) || glProgram.generation != generation) {
            throw new IllegalStateException("The material's program doesn't belong to this backend: " + material);
        }
        return glProgram;
    }

    // endregion

    // region render targets and post effects

    @Override
    public RenderTarget createTarget(int width, int height) {
        checkActive();
        return new GLRenderTarget(gl, width, height, generation);
    }

    @Override
    public int getScreenWidth() {
        return screenViewport()[2];
    }

    @Override
    public int getScreenHeight() {
        return screenViewport()[3];
    }

    private int[] screenViewport() {
        if (passTarget != null) {
            return savedViewport;
        }
        gl.getIntegerv(GL_VIEWPORT, viewport);
        return viewport;
    }

    @Override
    public PostProgram compilePost(String effectSource) {
        checkActive();
        return new GLPostProgram(gl, effectSource);
    }

    @Override
    public void drawPost(PostProgram program, PostParams params, RenderTarget destination, boolean blend) {
        checkActive();
        if (inPass) {
            throw new IllegalStateException("drawPost() called inside a pass.");
        }
        if (!(program instanceof GLPostProgram post)) {
            throw new IllegalArgumentException("Not a post program of this backend.");
        }
        GLRenderTarget target = destination != null ? glTarget(destination) : null;
        int width, height;
        if (target != null) {
            bindTarget(target);
            width = target.getWidth();
            height = target.getHeight();
        } else {
            int[] screen = screenViewport();
            width = screen[2];
            height = screen[3];
        }
        if (blend) {
            gl.blendFuncSeparate(GL_ONE, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA); // premultiplied over
        } else {
            gl.disable(GL_BLEND);
        }

        post.bind();
        Texture source = params.input(0);
        gl.uniform1f(post.flipYLocation, target == null ? 1f : 0f);
        if (source != null) {
            gl.uniform2f(post.texelLocation, 1f / source.getWidth(), 1f / source.getHeight());
        }
        gl.uniform2f(post.resolutionLocation, width, height);
        for (int i = 0; i < PostParams.MAX_INPUTS; i++) {
            Texture input = params.input(i);
            if (input != null) {
                gl.activeTexture(GL_TEXTURE0 + i);
                gl.bindTexture(GL_TEXTURE_2D, textureId(input));
            }
        }
        for (int u = 0, n = params.uniformCount(); u < n; u++) {
            int location = post.location(params.uniformName(u));
            if (location < 0) {
                continue;
            }
            switch (params.uniformSize(u)) {
                case 1 -> gl.uniform1f(location, params.uniformValue(u, 0));
                case 2 -> gl.uniform2f(location, params.uniformValue(u, 0), params.uniformValue(u, 1));
                default -> gl.uniform4f(location, params.uniformValue(u, 0), params.uniformValue(u, 1),
                        params.uniformValue(u, 2), params.uniformValue(u, 3));
            }
        }
        gl.bindVertexArray(emptyVao);
        gl.drawArrays(GL_TRIANGLES, 0, 3);
        stats.recordDrawCall(1, 3);

        gl.bindVertexArray(0);
        gl.activeTexture(GL_TEXTURE0);
        gl.useProgram(0);
        gl.enable(GL_BLEND);
        gl.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        Arrays.fill(boundTextures, -1); // units 0..3 changed
        if (target != null) {
            restoreFramebuffer();
        }
    }

    @Override
    public byte[] readPixels(RenderTarget target) {
        checkActive();
        if (inPass) {
            throw new IllegalStateException("readPixels() called inside a pass.");
        }
        GLRenderTarget glTarget = glTarget(target);
        int width = glTarget.getWidth(), height = glTarget.getHeight();
        ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
        int previous = gl.getInteger(GL_FRAMEBUFFER_BINDING);
        gl.bindFramebuffer(GL_FRAMEBUFFER, glTarget.framebuffer());
        gl.readPixels(0, 0, width, height, pixels); // targets store rows top-down: already in image order
        gl.bindFramebuffer(GL_FRAMEBUFFER, previous);
        byte[] out = new byte[width * height * 4];
        pixels.get(out);
        return out;
    }

    private GLRenderTarget glTarget(RenderTarget target) {
        if (!(target instanceof GLRenderTarget glTarget) || glTarget.isDisposed()) {
            throw new IllegalArgumentException("Not a live render target of this backend: " + target);
        }
        glTarget.ensureGeneration(generation); // made in a lost context: allocate again
        return glTarget;
    }

    private void bindTarget(GLRenderTarget target) {
        savedFramebuffer = gl.getInteger(GL_FRAMEBUFFER_BINDING);
        gl.getIntegerv(GL_VIEWPORT, savedViewport);
        gl.bindFramebuffer(GL_FRAMEBUFFER, target.framebuffer());
        gl.viewport(0, 0, target.getWidth(), target.getHeight());
    }

    private void restoreFramebuffer() {
        gl.bindFramebuffer(GL_FRAMEBUFFER, savedFramebuffer);
        gl.viewport(savedViewport[0], savedViewport[1], savedViewport[2], savedViewport[3]);
    }

    // endregion

    @Override
    public void dispose() {
        if (state.isDisposed()) {
            return;
        }
        state = State.DISPOSED;
        spritePipeline.dispose();
        shapePipeline.dispose();
        gl.deleteBuffer(ebo);
        gl.deleteBuffer(clipUbo);
        gl.deleteBuffer(styleUbo);
        gl.deleteTexture(whiteTexture);
        gl.deleteVertexArray(emptyVao);
    }

    /**
     * Upload the pass's clip and style tables. Each buffer gets one full block of headroom after the last slice, so
     * binding {@code blockBytes} at any batch offset stays inside the buffer.
     */
    private void uploadTables(BatchList batches) {
        boolean hasShapes = false;
        for (int b = 0, n = batches.size(); b < n && !hasShapes; b++) {
            hasShapes = batches.layout(b) == VertexLayout.FULL;
        }
        if (!hasShapes) {
            return;
        }
        uploadTable(clipUbo, batches.clipData(), batches.clipDataSize(), clipBlockBytes);
        uploadTable(styleUbo, batches.styleData(), batches.styleDataSize(), styleBlockBytes);
    }

    private void uploadTable(int ubo, float[] data, int floats, int blockBytes) {
        gl.bindBuffer(GL_UNIFORM_BUFFER, ubo);
        // orphan, sized so that binding a full block at the last batch offset stays inside the buffer
        gl.bufferData(GL_UNIFORM_BUFFER, (long) floats * Float.BYTES + blockBytes, GL_STREAM_DRAW);
        if (floats > 0) {
            if (tableUpload.capacity() < floats) {
                tableUpload = directFloats(Math.max(floats, tableUpload.capacity() * 2));
            }
            tableUpload.clear();
            tableUpload.put(data, 0, floats).flip(); // only the used prefix
            gl.bufferSubData(GL_UNIFORM_BUFFER, 0, tableUpload);
        }
        stats.recordUpload((long) floats * Float.BYTES);
    }

    private void bindTexture(int unit, int textureId) {
        if (boundTextures[unit] != textureId) {
            gl.activeTexture(GL_TEXTURE0 + unit);
            gl.bindTexture(GL_TEXTURE_2D, textureId);
            boundTextures[unit] = textureId;
            stats.recordTextureBind();
        }
    }

    private static int textureId(Texture texture) {
        if (texture instanceof GLTexture glTexture) {
            return glTexture.getId();
        }
        throw new IllegalArgumentException("Unsupported texture type for OpenGL: " + texture.getClass().getName());
    }

    private void checkActive() {
        if (state.isDisposed()) {
            throw new IllegalStateException("Render backend has been disposed.");
        }
    }

    private void configureSpriteAttributes() {
        int stride = VertexLayout.SPRITE.stride();
        gl.enableVertexAttribArray(0);
        gl.vertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        gl.enableVertexAttribArray(1);
        gl.vertexAttribPointer(1, 2, GL_FLOAT, false, stride, 8);
        gl.enableVertexAttribArray(2);
        gl.vertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, stride, 16);
        gl.enableVertexAttribArray(3);
        gl.vertexAttribIPointer(3, 1, GL_UNSIGNED_BYTE, stride, 20);
    }

    private void configureShapeAttributes() {
        int stride = VertexLayout.FULL.stride();
        gl.enableVertexAttribArray(0);
        gl.vertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        gl.enableVertexAttribArray(1);
        gl.vertexAttribPointer(1, 2, GL_FLOAT, false, stride, 8);
        gl.enableVertexAttribArray(2);
        gl.vertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, stride, 16);
        gl.enableVertexAttribArray(3);
        gl.vertexAttribPointer(3, 2, GL_FLOAT, false, stride, 20);
        gl.enableVertexAttribArray(4);
        gl.vertexAttribPointer(4, 4, GL_FLOAT, false, stride, 28);
        gl.enableVertexAttribArray(5);
        gl.vertexAttribIPointer(5, 1, GL_UNSIGNED_BYTE, stride, 44);
        gl.enableVertexAttribArray(6);
        gl.vertexAttribIPointer(6, 1, GL_UNSIGNED_BYTE, stride, 45);
        gl.enableVertexAttribArray(7);
        gl.vertexAttribIPointer(7, 1, GL_UNSIGNED_SHORT, stride, 46);
        gl.enableVertexAttribArray(8);
        gl.vertexAttribIPointer(8, 1, GL_UNSIGNED_SHORT, stride, 48);
    }

    private static FloatBuffer directFloats(int count) {
        return ByteBuffer.allocateDirect(count * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    private static ShortBuffer createQuadIndices(int quads) {
        ShortBuffer indices = ByteBuffer.allocateDirect(quads * 6 * Short.BYTES).order(ByteOrder.nativeOrder())
                .asShortBuffer();
        for (int q = 0; q < quads; q++) {
            int v = q * 4;
            indices.put((short) v).put((short) (v + 1)).put((short) (v + 2))
                    .put((short) (v + 2)).put((short) (v + 3)).put((short) v);
        }
        return indices.flip();
    }

    private int createWhiteTexture() {
        ByteBuffer pixel = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder());
        pixel.put((byte) 255).put((byte) 255).put((byte) 255).put((byte) 255).flip();
        int id = gl.genTexture();
        gl.bindTexture(GL_TEXTURE_2D, id);
        gl.texImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, pixel);
        gl.texParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        gl.texParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        gl.texParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        gl.texParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        gl.bindTexture(GL_TEXTURE_2D, 0);
        return id;
    }

    /**
     * Built-in program, vertex array and vertex buffer for one vertex layout, plus the quad range currently uploaded.
     */
    private final class LayoutPipeline {
        private final VertexLayout layout;
        final ProgramState program;
        private final int vao;
        private final int vbo;
        int chunkStart = -1;
        private int chunkEnd = -1;

        /**
         * Creates the objects and leaves the VAO and VBO bound so the caller can configure attributes.
         */
        LayoutPipeline(VertexLayout layout, GLProgram program, int slots, int ebo) {
            this.layout = layout;
            this.program = new ProgramState(gl, program, slots);
            this.vao = gl.genVertexArray();
            this.vbo = gl.genBuffer();
            gl.bindVertexArray(vao);
            gl.bindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo); // element binding is VAO state
            gl.bindBuffer(GL_ARRAY_BUFFER, vbo);
        }

        /** Bind the vertex array and buffer (the program is bound per batch). */
        void bind() {
            gl.bindVertexArray(vao);
            gl.bindBuffer(GL_ARRAY_BUFFER, vbo);
        }

        void resetChunk() {
            chunkStart = -1;
            chunkEnd = -1;
        }

        /**
         * Make sure quads {@code [first, first + count)} are in the uploaded chunk (must be bound).
         */
        void ensureUploaded(VertexStream vertices, int first, int count) {
            if (chunkStart >= 0 && first >= chunkStart && first + count <= chunkEnd) {
                return;
            }
            chunkStart = first;
            chunkEnd = Math.min(vertices.quadCount(), first + MAX_QUADS_PER_BATCH);
            // a range of the stream's buffer: no buffer views are created (allocation-free per frame)
            int stride = layout.stride();
            int offset = chunkStart * 4 * stride;
            int bytes = (chunkEnd - chunkStart) * 4 * stride;
            stats.recordUpload(bytes);
            gl.bufferData(GL_ARRAY_BUFFER, vertices.buffer(), offset, bytes, GL_STREAM_DRAW); // orphans
        }

        void dispose() {
            program.dispose();
            gl.deleteBuffer(vbo);
            gl.deleteVertexArray(vao);
        }
    }
}
