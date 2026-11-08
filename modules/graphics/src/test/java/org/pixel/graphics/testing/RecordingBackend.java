package org.pixel.graphics.testing;

import org.pixel.content.Texture;
import org.pixel.graphics.backend.BackendCapabilities;
import org.pixel.graphics.backend.BatchList;
import org.pixel.graphics.backend.MaterialProgram;
import org.pixel.graphics.material.Material;
import org.pixel.graphics.backend.PassState;
import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.backend.RenderData;
import org.pixel.graphics.backend.VertexLayout;
import org.pixel.graphics.backend.VertexStream;
import org.pixel.graphics.render.BlendMode;

import java.nio.ByteBuffer;
import org.pixel.commons.Color;
import org.pixel.graphics.backend.PostParams;
import org.pixel.graphics.backend.PostProgram;
import org.pixel.graphics.backend.RenderTarget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fake {@link RenderBackend} that records what would be sent to the GPU. With {@code capture = false} it only
 * counts, so it can be used in allocation tests.
 */
public class RecordingBackend implements RenderBackend {

    public record Batch(VertexLayout layout, int firstQuad, int quadCount, List<Texture> textures,
                        List<float[]> clips, List<float[]> styles, Material material, int slotLimit) {
    }

    public record Vertex(float x, float y, float u, float v, int rgba, int slot) {
    }

    public record FullVertex(float x, float y, float u, float v, int rgba, float localX, float localY,
                             float p0, float p1, float p2, float p3, int type, int slot, int clip, int style) {
    }

    public record Pass(BlendMode blendMode, List<Batch> batches, List<Vertex> vertices, List<FullVertex> shapes,
                       float[] viewMatrix, RenderTarget target) {
        /** Translation x of the view matrix (column-major m[3][0]): identifies which view drew the pass. */
        public float viewTranslationX() {
            return viewMatrix[12];
        }
    }

    private final BackendCapabilities capabilities;
    private final boolean capture;
    public final List<Pass> passes = new ArrayList<>();
    public int passCount;
    public long drawCalls;
    private boolean inPass;
    private BlendMode blendMode;
    private final float[] viewMatrix = new float[16];
    private RenderTarget target;
    /** Target and post events in order, e.g. "clear scene", "post fx scene->screen blend". */
    public final List<String> events = new ArrayList<>();
    public int screenWidth = 800;
    public int screenHeight = 600;

    public RecordingBackend(int maxTextureSlots, int maxQuadsPerBatch, boolean capture) {
        this(new BackendCapabilities(maxTextureSlots, maxQuadsPerBatch, 256, 256, 64), capture);
    }

    public RecordingBackend(BackendCapabilities capabilities, boolean capture) {
        this.capabilities = capabilities;
        this.capture = capture;
    }

    @Override
    public long getDrawCallCount() {
        return drawCalls;
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
        target = state.getTarget();
        if (state.isClearTarget() && capture) {
            events.add("clear " + target);
        }
        float[][] m = state.getViewMatrix().toUnsafeArray();
        for (int i = 0; i < 4; i++) {
            System.arraycopy(m[i], 0, viewMatrix, i * 4, 4);
        }
    }

    @Override
    public void submit(RenderData data) {
        if (!inPass) throw new IllegalStateException("submit outside pass");
        BatchList batches = data.batches();
        drawCalls += batches.size();
        passCount++;
        for (int b = 0; b < batches.size(); b++) {
            if (batches.material(b) != null) {
                batches.material(b).program(this); // like a real backend: compiled on first use
            }
        }
        if (!capture) {
            return;
        }
        List<Batch> recordedBatches = new ArrayList<>();
        for (int b = 0; b < batches.size(); b++) {
            List<Texture> textures = new ArrayList<>();
            for (int s = 0; s < batches.textureCount(b); s++) {
                if (batches.texture(b, s) != null) { // sticky slots can leave holes
                    textures.add(batches.texture(b, s));
                }
            }
            List<float[]> clips = new ArrayList<>();
            for (int c = 0; c < batches.clipCount(b); c++) {
                int o = batches.clipOffset(b) + c * BatchList.CLIP_FLOATS;
                clips.add(Arrays.copyOfRange(batches.clipData(), o, o + BatchList.CLIP_FLOATS));
            }
            List<float[]> styles = new ArrayList<>();
            for (int s = 0; s < batches.styleCount(b); s++) {
                int o = batches.styleOffset(b) + s * BatchList.STYLE_FLOATS;
                styles.add(Arrays.copyOfRange(batches.styleData(), o, o + BatchList.STYLE_FLOATS));
            }
            recordedBatches.add(new Batch(batches.layout(b), batches.firstQuad(b), batches.quadCount(b), textures,
                    clips, styles, batches.material(b), batches.slotLimit(b)));
        }
        passes.add(new Pass(blendMode, recordedBatches, readSprites(data.vertices(VertexLayout.SPRITE)),
                readShapes(data.vertices(VertexLayout.FULL)), viewMatrix.clone(), target));
    }

    @Override
    public void endPass() {
        if (!inPass) throw new IllegalStateException("endPass without beginPass");
        inPass = false;
    }

    @Override
    public void dispose() {
    }

    // region targets and post effects

    /** A target that only records its name and size. */
    public static final class Target implements RenderTarget {
        public final String name;
        private final TestTexture texture;
        private final Color clearColor = new Color(0f, 0f, 0f, 0f);
        public boolean disposed;

        Target(String name, int width, int height) {
            this.name = name;
            this.texture = new TestTexture(width, height);
        }

        @Override
        public int getWidth() {
            return texture.getWidth();
        }

        @Override
        public int getHeight() {
            return texture.getHeight();
        }

        @Override
        public Texture getTexture() {
            return texture;
        }

        @Override
        public void resize(int width, int height) {
            texture.setWidth(width);
            texture.setHeight(height);
        }

        @Override
        public Color getClearColor() {
            return clearColor;
        }

        @Override
        public void setClearColor(Color color) {
            clearColor.set(color);
        }

        @Override
        public void dispose() {
            disposed = true;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /** A post program that only records its source. */
    public record Program(String source) implements PostProgram {
        @Override
        public void dispose() {
        }
    }

    public final List<Target> targets = new ArrayList<>();

    @Override
    public RenderTarget createTarget(int width, int height) {
        Target target = new Target("t" + targets.size(), width, height);
        targets.add(target);
        return target;
    }

    @Override
    public int getScreenWidth() {
        return screenWidth;
    }

    @Override
    public int getScreenHeight() {
        return screenHeight;
    }

    public int contextGeneration;

    @Override
    public int getContextGeneration() {
        return contextGeneration;
    }

    public int compiledPrograms;

    @Override
    public PostProgram compilePost(String effectSource) {
        compiledPrograms++;
        return new Program(effectSource);
    }

    @Override
    public void drawPost(PostProgram program, PostParams params, RenderTarget destination, boolean blend) {
        if (inPass) throw new IllegalStateException("drawPost inside a pass");
        drawCalls++;
        if (capture) {
            String source = params.input(0) == null ? "?" : sourceName(params.input(0));
            events.add("post " + source + "->" + (destination == null ? "screen" : destination) + (blend ? " blend" : ""));
        }
    }

    private String sourceName(Texture texture) {
        for (Target t : targets) {
            if (t.getTexture() == texture) {
                return t.name;
            }
        }
        return "texture";
    }

    @Override
    public byte[] readPixels(RenderTarget target) {
        return new byte[target.getWidth() * target.getHeight() * 4];
    }

    // endregion

    @Override
    public Texture createTexture(int width, int height, ByteBuffer pixels, boolean smooth) {
        return new TestTexture(width, height);
    }

    // region materials

    /** A material program that only records its source. */
    public static final class CompiledMaterial implements MaterialProgram {
        public final String source;
        public boolean disposed;

        CompiledMaterial(String source) {
            this.source = source;
        }

        @Override
        public void dispose() {
            disposed = true;
        }
    }

    public final List<CompiledMaterial> compiledMaterials = new ArrayList<>();
    /** Sources containing this text fail to compile. */
    public String failOn;

    @Override
    public MaterialProgram compileMaterial(Material material, String vertexSource, String source) {
        if (failOn != null && source.contains(failOn)) {
            throw new IllegalStateException("0:1: error: '" + failOn + "'");
        }
        CompiledMaterial compiled = new CompiledMaterial(source);
        compiledMaterials.add(compiled);
        return compiled;
    }

    // endregion

    public Pass lastPass() {
        return passes.get(passes.size() - 1);
    }

    private static List<Vertex> readSprites(VertexStream stream) {
        List<Vertex> out = new ArrayList<>();
        ByteBuffer data = stream.slice(0, stream.vertexCount());
        for (int i = 0; i < stream.vertexCount(); i++) {
            int base = i * VertexLayout.SPRITE.stride();
            out.add(new Vertex(data.getFloat(base), data.getFloat(base + 4), data.getFloat(base + 8),
                    data.getFloat(base + 12), littleEndianInt(data, base + 16), data.get(base + 20) & 0xFF));
        }
        return out;
    }

    private static List<FullVertex> readShapes(VertexStream stream) {
        List<FullVertex> out = new ArrayList<>();
        ByteBuffer data = stream.slice(0, stream.vertexCount());
        for (int i = 0; i < stream.vertexCount(); i++) {
            int b = i * VertexLayout.FULL.stride();
            out.add(new FullVertex(data.getFloat(b), data.getFloat(b + 4), data.getFloat(b + 8),
                    data.getFloat(b + 12), littleEndianInt(data, b + 16), data.getFloat(b + 20),
                    data.getFloat(b + 24), data.getFloat(b + 28), data.getFloat(b + 32), data.getFloat(b + 36),
                    data.getFloat(b + 40), data.get(b + 44) & 0xFF, data.get(b + 45) & 0xFF,
                    data.getShort(b + 46) & 0xFFFF, data.getShort(b + 48) & 0xFFFF));
        }
        return out;
    }

    private static int littleEndianInt(ByteBuffer data, int offset) {
        return (data.get(offset) & 0xFF) | (data.get(offset + 1) & 0xFF) << 8
                | (data.get(offset + 2) & 0xFF) << 16 | (data.get(offset + 3) & 0xFF) << 24;
    }
}
