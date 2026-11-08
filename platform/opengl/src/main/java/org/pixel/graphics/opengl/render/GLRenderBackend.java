package org.pixel.graphics.opengl.render;

import org.pixel.commons.lifecycle.State;
import org.pixel.content.Texture;
import org.pixel.content.opengl.GLTexture;
import org.pixel.graphics.RenderStats;
import org.pixel.graphics.backend.BackendCapabilities;
import org.pixel.graphics.backend.BatchList;
import org.pixel.graphics.backend.PassState;
import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.backend.RenderData;
import org.pixel.graphics.backend.VertexLayout;
import org.pixel.graphics.backend.VertexStream;
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
 * bind their slice of the pass's clip and style tables as uniform buffer ranges.
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
    private static final int MAX_STYLES = 128;  // 14 KB uniform block (7 vec4 per style; GLES 3.0 guarantees 16 KB)

    private final GLApi gl;
    private final RenderStats stats;
    private final BackendCapabilities capabilities;
    // GL objects: created with the context, and again by contextRecreated()
    private LayoutPipeline spritePipeline;
    private LayoutPipeline shapePipeline;
    private int ebo;
    private int clipUbo;
    private int styleUbo;
    private final int clipBlockBytes = MAX_CLIPS * BatchList.CLIP_FLOATS * Float.BYTES;
    private final int styleBlockBytes = MAX_STYLES * BatchList.STYLE_FLOATS * Float.BYTES;
    private int whiteTexture;
    private final int[] boundTextures;
    private final FloatBuffer matrixBuffer = directFloats(16);
    private FloatBuffer tableUpload = directFloats(1024); // staging for clip/style tables

    private State state = State.INITIALIZED;
    private boolean inPass;
    private LayoutPipeline current;

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
        createGlObjects(); // the old names died with their context: nothing to delete
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
        inPass = true;

        matrixBuffer.clear();
        passState.getViewMatrix().writeBuffer(matrixBuffer);
        spritePipeline.setMatrix(matrixBuffer);
        shapePipeline.setMatrix(matrixBuffer);
        current = null;

        switch (passState.getBlendMode()) {
            case ADDITIVE -> gl.blendFunc(GL_ONE, GL_ONE);
            case MULTIPLY -> gl.blendFunc(GL_DST_COLOR, GL_ZERO);
            default -> gl.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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
            if (pipeline != current) {
                pipeline.bind();
                current = pipeline;
            }
            pipeline.ensureUploaded(data.vertices(batches.layout(b)), first, count);

            for (int slot = 0, t = batches.textureCount(b); slot < t; slot++) {
                bindTexture(slot, textureId(batches.texture(b, slot)));
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

        // restore the shared defaults other renderers rely on
        gl.bindVertexArray(0);
        gl.bindBuffer(GL_ARRAY_BUFFER, 0);
        gl.bindBuffer(GL_UNIFORM_BUFFER, 0);
        gl.activeTexture(GL_TEXTURE0);
        gl.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        gl.useProgram(0);
    }

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
     * Program, vertex array and vertex buffer for one vertex layout, plus the quad range currently uploaded.
     */
    private final class LayoutPipeline {
        private final VertexLayout layout;
        private final GLProgram program;
        private final int uMatrixLocation;
        private final int vao;
        private final int vbo;
        int chunkStart = -1;
        private int chunkEnd = -1;

        /**
         * Creates the objects and leaves the VAO and VBO bound so the caller can configure attributes.
         */
        LayoutPipeline(VertexLayout layout, GLProgram program, int slots, int ebo) {
            this.layout = layout;
            this.program = program;
            this.uMatrixLocation = program.uniformLocation(GLSpriteProgram.U_MATRIX);
            program.bind();
            int[] samplerUnits = new int[slots];
            for (int i = 0; i < slots; i++) {
                samplerUnits[i] = i; // uTextures[i] samples texture unit i
            }
            gl.uniform1iv(program.uniformLocation(GLSpriteProgram.U_TEXTURES), samplerUnits);
            program.unbind();

            this.vao = gl.genVertexArray();
            this.vbo = gl.genBuffer();
            gl.bindVertexArray(vao);
            gl.bindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo); // element binding is VAO state
            gl.bindBuffer(GL_ARRAY_BUFFER, vbo);
        }

        void setMatrix(FloatBuffer matrix) {
            program.bind();
            gl.uniformMatrix4fv(uMatrixLocation, matrix);
        }

        void bind() {
            program.bind();
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
