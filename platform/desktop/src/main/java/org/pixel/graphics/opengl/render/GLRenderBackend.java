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
import org.pixel.graphics.backend.VertexLayout;
import org.pixel.graphics.backend.VertexStream;

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

/**
 * OpenGL 3.3 implementation of the URP v2 {@link RenderBackend}.
 * <p>
 * Quads are drawn indexed from a static index buffer ({@code 0,1,2, 2,3,0} per quad, 16-bit indices). Vertices of a
 * pass are uploaded in chunks of at most {@link #MAX_QUADS_PER_BATCH} quads (one upload in the common case) and each
 * batch is drawn with an index offset into its chunk, so no base-vertex draw call is needed (GLES 3.0 compatible).
 * <p>
 * Owned and disposed by the graphics device; requires a current GL context.
 */
public final class GLRenderBackend implements RenderBackend {

    /**
     * 16384 quads = 65536 vertices, the most a 16-bit index can address.
     */
    public static final int MAX_QUADS_PER_BATCH = 16384;
    private static final int MAX_TEXTURE_SLOTS = 16;

    private final RenderStats stats;
    private final BackendCapabilities capabilities;
    private final GLSpriteProgram program;
    private final int uMatrixLocation;
    private final int vao;
    private final int vbo;
    private final int ebo;
    private final int whiteTexture;
    private final int[] boundTextures;
    private final FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(16);

    private State state = State.INITIALIZED;
    private boolean inPass;

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

        int units = glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS);
        int slots = Math.max(1, Math.min(units, MAX_TEXTURE_SLOTS));
        this.capabilities = new BackendCapabilities(slots, MAX_QUADS_PER_BATCH);
        this.boundTextures = new int[slots];

        this.program = new GLSpriteProgram(slots);
        this.uMatrixLocation = program.getUniformLocation(GLSpriteProgram.U_MATRIX);
        program.bind();
        int[] samplerUnits = new int[slots];
        for (int i = 0; i < slots; i++) {
            samplerUnits[i] = i; // uTextures[i] samples texture unit i
        }
        glUniform1iv(program.getUniformLocation(GLSpriteProgram.U_TEXTURES), samplerUnits);
        program.unbind();

        this.whiteTexture = createWhiteTexture();

        this.vao = glGenVertexArrays();
        this.vbo = glGenBuffers();
        this.ebo = glGenBuffers();
        glBindVertexArray(vao);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo); // element binding is VAO state
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, createQuadIndices(MAX_QUADS_PER_BATCH), GL_STATIC_DRAW);

        int stride = VertexLayout.SPRITE.stride();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 8);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, stride, 16);
        glEnableVertexAttribArray(3);
        glVertexAttribIPointer(3, 1, GL_UNSIGNED_BYTE, stride, 20);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
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

        program.bind();
        matrixBuffer.clear();
        passState.getViewMatrix().writeBuffer(matrixBuffer);
        glUniformMatrix4fv(uMatrixLocation, false, matrixBuffer);

        switch (passState.getBlendMode()) {
            case ADDITIVE -> glBlendFunc(GL_ONE, GL_ONE);
            case MULTIPLY -> glBlendFunc(GL_DST_COLOR, GL_ZERO);
            default -> glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // Other renderers may have changed texture bindings since the last pass: forget what we think is bound, and
        // make sure every unit the program samples has a valid texture (some drivers, e.g. macOS, complain otherwise).
        Arrays.fill(boundTextures, -1);
        for (int unit = 0; unit < boundTextures.length; unit++) {
            bindTexture(unit, whiteTexture);
        }
    }

    @Override
    public void submit(VertexStream vertices, BatchList batches) {
        checkActive();
        if (!inPass) {
            throw new IllegalStateException("submit() called outside beginPass()/endPass().");
        }
        if (vertices.getLayout() != VertexLayout.SPRITE) {
            throw new IllegalArgumentException("Unsupported vertex layout: " + vertices.getLayout());
        }

        int totalQuads = vertices.quadCount();
        int chunkStart = -1;
        int chunkEnd = -1;
        for (int b = 0, n = batches.size(); b < n; b++) {
            int first = batches.firstQuad(b);
            int count = batches.quadCount(b);
            if (count == 0) {
                continue;
            }
            if (count > MAX_QUADS_PER_BATCH) {
                throw new IllegalArgumentException("Batch exceeds " + MAX_QUADS_PER_BATCH + " quads.");
            }

            if (chunkStart < 0 || first + count > chunkEnd) {
                chunkStart = first;
                chunkEnd = Math.min(totalQuads, first + MAX_QUADS_PER_BATCH);
                // upload by address: no ByteBuffer views are created (allocation-free per frame)
                int stride = VertexLayout.SPRITE.stride();
                long offset = (long) chunkStart * 4 * stride;
                long bytes = (long) (chunkEnd - chunkStart) * 4 * stride;
                stats.recordUpload(bytes);
                nglBufferData(GL_ARRAY_BUFFER, bytes, MemoryUtil.memAddress0(vertices.buffer()) + offset,
                        GL_STREAM_DRAW); // orphans the previous storage
            }

            for (int slot = 0, t = batches.textureCount(b); slot < t; slot++) {
                bindTexture(slot, textureId(batches.texture(b, slot)));
            }

            glDrawElements(GL_TRIANGLES, count * 6, GL_UNSIGNED_SHORT, (long) (first - chunkStart) * 6 * Short.BYTES);
            stats.recordDrawCall(count, count * 4L);
        }
    }

    @Override
    public void endPass() {
        if (!inPass) {
            throw new IllegalStateException("endPass() called without beginPass().");
        }
        inPass = false;

        // restore the shared defaults other renderers rely on
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glActiveTexture(GL_TEXTURE0);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        program.unbind();
    }

    @Override
    public void dispose() {
        if (state.isDisposed()) {
            return;
        }
        state = State.DISPOSED;
        program.dispose();
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(vao);
        glDeleteTextures(whiteTexture);
        MemoryUtil.memFree(matrixBuffer);
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
}
