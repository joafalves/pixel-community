package org.pixel.graphics.testing;

import org.pixel.content.Texture;
import org.pixel.graphics.backend.BackendCapabilities;
import org.pixel.graphics.backend.BatchList;
import org.pixel.graphics.backend.PassState;
import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.backend.VertexStream;
import org.pixel.graphics.render.BlendMode;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Fake {@link RenderBackend} that records what would be sent to the GPU. With {@code capture = false} it only
 * counts, so it can be used in allocation tests.
 */
public class RecordingBackend implements RenderBackend {

    public record Batch(int firstQuad, int quadCount, List<Texture> textures) {
    }

    public record Vertex(float x, float y, float u, float v, int rgba, int slot) {
    }

    public record Pass(BlendMode blendMode, List<Batch> batches, List<Vertex> vertices) {
    }

    private final BackendCapabilities capabilities;
    private final boolean capture;
    public final List<Pass> passes = new ArrayList<>();
    public int passCount;
    public long drawCalls;
    private boolean inPass;
    private BlendMode blendMode;

    public RecordingBackend(int maxTextureSlots, int maxQuadsPerBatch, boolean capture) {
        this.capabilities = new BackendCapabilities(maxTextureSlots, maxQuadsPerBatch);
        this.capture = capture;
    }

    @Override
    public BackendCapabilities capabilities() {
        return capabilities;
    }

    @Override
    public void beginPass(PassState state) {
        if (inPass) throw new IllegalStateException("nested pass");
        inPass = true;
        blendMode = state.getBlendMode();
    }

    @Override
    public void submit(VertexStream vertices, BatchList batches) {
        if (!inPass) throw new IllegalStateException("submit outside pass");
        drawCalls += batches.size();
        passCount++;
        if (!capture) {
            return;
        }
        List<Batch> recordedBatches = new ArrayList<>();
        for (int b = 0; b < batches.size(); b++) {
            List<Texture> textures = new ArrayList<>();
            for (int s = 0; s < batches.textureCount(b); s++) {
                textures.add(batches.texture(b, s));
            }
            recordedBatches.add(new Batch(batches.firstQuad(b), batches.quadCount(b), textures));
        }
        List<Vertex> recordedVertices = new ArrayList<>();
        ByteBuffer data = vertices.slice(0, vertices.vertexCount());
        for (int i = 0; i < vertices.vertexCount(); i++) {
            int base = i * 24;
            recordedVertices.add(new Vertex(data.getFloat(base), data.getFloat(base + 4), data.getFloat(base + 8),
                    data.getFloat(base + 12), littleEndianInt(data, base + 16), data.get(base + 20) & 0xFF));
        }
        passes.add(new Pass(blendMode, recordedBatches, recordedVertices));
    }

    @Override
    public void endPass() {
        if (!inPass) throw new IllegalStateException("endPass without beginPass");
        inPass = false;
    }

    @Override
    public void dispose() {
    }

    public Pass lastPass() {
        return passes.get(passes.size() - 1);
    }

    private static int littleEndianInt(ByteBuffer data, int offset) {
        return (data.get(offset) & 0xFF) | (data.get(offset + 1) & 0xFF) << 8
                | (data.get(offset + 2) & 0xFF) << 16 | (data.get(offset + 3) & 0xFF) << 24;
    }
}
