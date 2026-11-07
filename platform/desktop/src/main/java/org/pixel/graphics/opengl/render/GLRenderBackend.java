package org.pixel.graphics.opengl.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;
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
import org.pixel.graphics.shader.opengl.GLShader;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL31C.GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT;

/**
 * OpenGL 3.3 implementation of the URP v2 {@link RenderBackend}.
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
    private static final int MAX_STYLES = 256;  // 8 KB uniform block

    private final RenderStats stats;
    private final BackendCapabilities capabilities;
    private final LayoutPipeline spritePipeline;
    private final LayoutPipeline shapePipeline;
    private final int ebo;
    private final int clipUbo;
    private final int styleUbo;
    private final int clipBlockBytes = MAX_CLIPS * BatchList.CLIP_FLOATS * Float.BYTES;
    private final int styleBlockBytes = MAX_STYLES * BatchList.STYLE_FLOATS * Float.BYTES;
    private final int whiteTexture;
    private final int[] boundTextures;
    private final FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(16);

    private State state = State.INITIALIZED;
    private boolean inPass;
    private LayoutPipeline current;

    /**
     * Constructor (requires a current OpenGL 3.3 context).
     *
     * @param stats The device statistics to record GPU work into.
     */
    public GLRenderBackend(RenderStats stats) {
        if (stats == null) {
            throw new IllegalArgumentException("Render stats cannot be null.");
        }
        this.stats = stats;

        int slots = Math.max(1, Math.min(glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS), MAX_TEXTURE_SLOTS));
        int alignmentBytes = Math.max(16, glGetInteger(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT));
        int alignmentFloats = (alignmentBytes / Float.BYTES + 3) / 4 * 4; // whole vec4s
        this.capabilities = new BackendCapabilities(slots, MAX_QUADS_PER_BATCH, MAX_CLIPS, MAX_STYLES,
                alignmentFloats);
        this.boundTextures = new int[slots];
        this.whiteTexture = createWhiteTexture();

        this.ebo = glGenBuffers();
        this.spritePipeline = new LayoutPipeline(VertexLayout.SPRITE, new GLSpriteProgram(slots), slots, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, createQuadIndices(MAX_QUADS_PER_BATCH), GL_STATIC_DRAW);
        configureSpriteAttributes();
        this.shapePipeline = new LayoutPipeline(VertexLayout.FULL, new GLShapeProgram(slots, MAX_CLIPS, MAX_STYLES),
                slots, ebo);
        configureShapeAttributes();
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        this.clipUbo = glGenBuffers();
        this.styleUbo = glGenBuffers();
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
            case ADDITIVE -> glBlendFunc(GL_ONE, GL_ONE);
            case MULTIPLY -> glBlendFunc(GL_DST_COLOR, GL_ZERO);
            default -> glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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
                glBindBufferRange(GL_UNIFORM_BUFFER, GLShapeProgram.CLIP_BINDING, clipUbo,
                        (long) batches.clipOffset(b) * Float.BYTES, clipBlockBytes);
                glBindBufferRange(GL_UNIFORM_BUFFER, GLShapeProgram.STYLE_BINDING, styleUbo,
                        (long) batches.styleOffset(b) * Float.BYTES, styleBlockBytes);
            }

            glDrawElements(GL_TRIANGLES, count * 6, GL_UNSIGNED_SHORT,
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
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);
        glActiveTexture(GL_TEXTURE0);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glUseProgram(0);
    }

    @Override
    public void dispose() {
        if (state.isDisposed()) {
            return;
        }
        state = State.DISPOSED;
        spritePipeline.dispose();
        shapePipeline.dispose();
        glDeleteBuffers(ebo);
        glDeleteBuffers(clipUbo);
        glDeleteBuffers(styleUbo);
        glDeleteTextures(whiteTexture);
        MemoryUtil.memFree(matrixBuffer);
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
        long bytes = (long) data.length * Float.BYTES + blockBytes;
        glBindBuffer(GL_UNIFORM_BUFFER, ubo);
        nglBufferData(GL_UNIFORM_BUFFER, bytes, 0L, GL_STREAM_DRAW); // orphan
        if (floats > 0) {
            glBufferSubData(GL_UNIFORM_BUFFER, 0, data); // whole array (valid prefix + unused capacity)
        }
        stats.recordUpload((long) floats * Float.BYTES);
    }

    private void bindTexture(int unit, int textureId) {
        if (boundTextures[unit] != textureId) {
            glActiveTexture(GL_TEXTURE0 + unit);
            glBindTexture(GL_TEXTURE_2D, textureId);
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
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 8);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, stride, 16);
        glEnableVertexAttribArray(3);
        glVertexAttribIPointer(3, 1, GL_UNSIGNED_BYTE, stride, 20);
    }

    private void configureShapeAttributes() {
        int stride = VertexLayout.FULL.stride();
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 8);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, stride, 16);
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 2, GL_FLOAT, false, stride, 20);
        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 4, GL_FLOAT, false, stride, 28);
        glEnableVertexAttribArray(5);
        glVertexAttribIPointer(5, 1, GL_UNSIGNED_BYTE, stride, 44);
        glEnableVertexAttribArray(6);
        glVertexAttribIPointer(6, 1, GL_UNSIGNED_BYTE, stride, 45);
        glEnableVertexAttribArray(7);
        glVertexAttribIPointer(7, 1, GL_UNSIGNED_SHORT, stride, 46);
        glEnableVertexAttribArray(8);
        glVertexAttribIPointer(8, 1, GL_UNSIGNED_SHORT, stride, 48);
    }

    private static ShortBuffer createQuadIndices(int quads) {
        ShortBuffer indices = BufferUtils.createShortBuffer(quads * 6);
        for (int q = 0; q < quads; q++) {
            int v = q * 4;
            indices.put((short) v).put((short) (v + 1)).put((short) (v + 2))
                    .put((short) (v + 2)).put((short) (v + 3)).put((short) v);
        }
        return indices.flip();
    }

    private static int createWhiteTexture() {
        ByteBuffer pixel = BufferUtils.createByteBuffer(4);
        pixel.put((byte) 255).put((byte) 255).put((byte) 255).put((byte) 255).flip();
        int id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixel);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);
        return id;
    }

    /**
     * Program, vertex array and vertex buffer for one vertex layout, plus the quad range currently uploaded.
     */
    private final class LayoutPipeline {
        private final VertexLayout layout;
        private final GLShader program;
        private final int uMatrixLocation;
        private final int vao;
        private final int vbo;
        int chunkStart = -1;
        private int chunkEnd = -1;

        /**
         * Creates the objects and leaves the VAO and VBO bound so the caller can configure attributes.
         */
        LayoutPipeline(VertexLayout layout, GLShader program, int slots, int ebo) {
            this.layout = layout;
            this.program = program;
            this.uMatrixLocation = program.getUniformLocation(GLSpriteProgram.U_MATRIX);
            program.bind();
            int[] samplerUnits = new int[slots];
            for (int i = 0; i < slots; i++) {
                samplerUnits[i] = i; // uTextures[i] samples texture unit i
            }
            glUniform1iv(program.getUniformLocation(GLSpriteProgram.U_TEXTURES), samplerUnits);
            program.unbind();

            this.vao = glGenVertexArrays();
            this.vbo = glGenBuffers();
            glBindVertexArray(vao);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo); // element binding is VAO state
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
        }

        void setMatrix(FloatBuffer matrix) {
            program.bind();
            glUniformMatrix4fv(uMatrixLocation, false, matrix);
        }

        void bind() {
            program.bind();
            glBindVertexArray(vao);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
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
            // upload by address: no ByteBuffer views are created (allocation-free per frame)
            int stride = layout.stride();
            long offset = (long) chunkStart * 4 * stride;
            long bytes = (long) (chunkEnd - chunkStart) * 4 * stride;
            stats.recordUpload(bytes);
            nglBufferData(GL_ARRAY_BUFFER, bytes, MemoryUtil.memAddress0(vertices.buffer()) + offset,
                    GL_STREAM_DRAW); // orphans the previous storage
        }

        void dispose() {
            program.dispose();
            glDeleteBuffers(vbo);
            glDeleteVertexArrays(vao);
        }
    }
}
