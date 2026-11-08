package org.pixel.graphics.pass;

import org.pixel.graphics.backend.RenderTarget;
import org.pixel.graphics.draw.BatchBreak;
import org.pixel.graphics.draw.DrawList;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.draw.ShapeType;
import org.pixel.graphics.draw.StyleTable;
import org.pixel.graphics.material.Material;

import java.util.Arrays;

/**
 * What a {@link RenderPipeline} did in its last {@link RenderPipeline#render()}: per pass and per layer statistics,
 * the list of batches with the reason each one started, and the pickable commands
 * ({@code canvas.rect(...).pick(id)}) for {@link #pick}:
 * <pre>
 * FrameInfo frame = pipeline.lastFrame();
 * frame.getDrawCalls();                       // draw calls of the whole pipeline
 * for (FrameInfo.LayerStats layer : frame.getLayers()) {
 *     layer.getBatches(); layer.getBreaks(BatchBreak.TEXTURE_SLOTS); ...
 * }
 * int id = frame.pick(WORLD, worldX, worldY); // top-most pickable command at a point in layer coordinates
 * </pre>
 * The object is reused: it is updated in place by every {@code render()} (allocation-free once warmed up), so read
 * it between frames and copy what must be kept. Times are CPU time spent recording GPU work (GPU execution is
 * asynchronous).
 */
public final class FrameInfo {

    /**
     * The kind of a pass.
     */
    public enum PassKind {LAYERS, POST, CUSTOM}

    /**
     * Statistics of one pass.
     */
    public static final class PassStats {
        private final int index;
        private final PassKind kind;
        private final RenderTarget target;
        private final LayerStats[] layers;
        long drawCalls;
        long nanos;

        PassStats(int index, PassKind kind, RenderTarget target, LayerStats[] layers) {
            this.index = index;
            this.kind = kind;
            this.target = target;
            this.layers = layers;
        }

        /** Position of the pass in the pipeline (0 = first). */
        public int getIndex() {
            return index;
        }

        public PassKind getKind() {
            return kind;
        }

        /** The target it draws into, or {@code null} for the screen (custom passes: always {@code null}). */
        public RenderTarget getTarget() {
            return target;
        }

        /** The layers it draws (layer passes; empty for others). Shared array: don't modify. */
        public LayerStats[] getLayers() {
            return layers;
        }

        public long getDrawCalls() {
            return drawCalls;
        }

        /** CPU time of the pass, in nanoseconds. */
        public long getNanos() {
            return nanos;
        }
    }

    /**
     * Statistics of one layer as drawn by one pass (a layer drawn by two passes has two).
     */
    public static final class LayerStats {
        private final Layer layer;
        private final int pass;
        int commands;
        int culled;
        int batches;
        int quads;
        long drawCalls;
        final int[] breaks = new int[BatchBreak.values().length];

        LayerStats(Layer layer, int pass) {
            this.layer = layer;
            this.pass = pass;
        }

        public Layer getLayer() {
            return layer;
        }

        /** Index of the pass that drew it. */
        public int getPass() {
            return pass;
        }

        /** Commands recorded into the layer (each quad of a path or a text counts). */
        public int getCommands() {
            return commands;
        }

        /** Commands outside the pass's view (not drawn). */
        public int getCulled() {
            return culled;
        }

        public int getBatches() {
            return batches;
        }

        public int getQuads() {
            return quads;
        }

        public long getDrawCalls() {
            return drawCalls;
        }

        /** How many batches started for that reason ({@link BatchBreak#FIRST}: 1 if anything was drawn). */
        public int getBreaks(BatchBreak reason) {
            return breaks[reason.ordinal()];
        }

        void reset() {
            commands = culled = batches = quads = 0;
            drawCalls = 0;
            Arrays.fill(breaks, 0);
        }
    }

    private final PassStats[] passes;
    private final LayerStats[] layers;
    private long frameNumber;
    private long nanos;

    // batch list
    private int batchCount;
    private LayerStats[] batchLayer = new LayerStats[64];
    private BatchBreak[] batchReason = new BatchBreak[64];
    private Material[] batchMaterial = new Material[64];
    private int[] batchQuads = new int[64];
    private int[] batchTextures = new int[64];

    // pickable commands, per pickable layer
    private final Layer[] pickLayers;
    private final PickSet[] pickSets;

    FrameInfo(PassStats[] passes, LayerStats[] layers, Layer[] pickLayers) {
        this.passes = passes;
        this.layers = layers;
        this.pickLayers = pickLayers;
        this.pickSets = new PickSet[pickLayers.length];
        for (int i = 0; i < pickSets.length; i++) {
            pickSets[i] = new PickSet();
        }
    }

    // region stats

    /** Which frame this is (1 = the first {@code render()}; 0 = nothing rendered yet). */
    public long getFrameNumber() {
        return frameNumber;
    }

    /** CPU time of the whole {@code render()}, in nanoseconds. */
    public long getNanos() {
        return nanos;
    }

    /** The passes, in order. Shared array: don't modify. */
    public PassStats[] getPasses() {
        return passes;
    }

    /** Every (pass, layer) pair, in drawing order. Shared array: don't modify. */
    public LayerStats[] getLayers() {
        return layers;
    }

    public long getDrawCalls() {
        long total = 0;
        for (PassStats pass : passes) {
            total += pass.drawCalls;
        }
        return total;
    }

    /** Batches of all layer passes. */
    public int getBatchCount() {
        return batchCount;
    }

    /** Batches started for a reason, over all layer passes. */
    public int getBreaks(BatchBreak reason) {
        int total = 0;
        for (LayerStats layer : layers) {
            total += layer.breaks[reason.ordinal()];
        }
        return total;
    }

    /** The layer (and pass) a batch belongs to. Batches are numbered in drawing order. */
    public LayerStats getBatchLayer(int batch) {
        return batchLayer[java.util.Objects.checkIndex(batch, batchCount)];
    }

    /** Why the batch started. */
    public BatchBreak getBatchBreak(int batch) {
        return batchReason[java.util.Objects.checkIndex(batch, batchCount)];
    }

    /** The batch's material, or {@code null} for the built-in shader. */
    public Material getBatchMaterial(int batch) {
        return batchMaterial[java.util.Objects.checkIndex(batch, batchCount)];
    }

    public int getBatchQuads(int batch) {
        return batchQuads[java.util.Objects.checkIndex(batch, batchCount)];
    }

    public int getBatchTextures(int batch) {
        return batchTextures[java.util.Objects.checkIndex(batch, batchCount)];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Frame ").append(frameNumber).append(": ").append(getDrawCalls())
                .append(" draw calls, ").append(batchCount).append(" batches, ")
                .append(String.format(java.util.Locale.ROOT, "%.3f ms", nanos / 1e6));
        for (LayerStats layer : layers) {
            sb.append("\n  pass ").append(layer.pass).append(' ').append(layer.layer.getName()).append(": ")
                    .append(layer.commands).append(" commands (").append(layer.culled).append(" culled), ")
                    .append(layer.batches).append(" batches");
            for (BatchBreak reason : BatchBreak.values()) {
                int n = layer.breaks[reason.ordinal()];
                if (n > 0 && reason != BatchBreak.FIRST) {
                    sb.append(", ").append(n).append(' ').append(reason.getDescription());
                }
            }
        }
        return sb.toString();
    }

    // endregion

    // region picking

    /**
     * The pick id of the top-most pickable command of a layer at a point, or 0. What hits:
     * <ul>
     *   <li>shapes (rectangles, ellipses, lines): inside their outline or stroke, filled or not (an outlined circle
     *   is picked anywhere inside it), except in the gaps of dashes; never their shadow;</li>
     *   <li>paths: inside their fill and their stroke (an unfilled path only on its stroke);</li>
     *   <li>text: the boxes of its glyphs (text drawn only as a shadow isn't pickable);</li>
     *   <li>images, nine-patches and sprites: their rectangle (transparent pixels included).</li>
     * </ul>
     * Clipped parts don't hit. The commands are those of the last frame, as they were drawn.
     *
     * @param layer The layer (drawn by this pipeline).
     * @param x     X in layer coordinates (e.g. a camera's world coordinates).
     * @param y     Y in layer coordinates.
     */
    public int pick(Layer layer, float x, float y) {
        PickSet set = pickSet(layer);
        for (int i = set.size - 1; i >= 0; i--) {
            if (set.hit(i, x, y)) {
                return set.ids[i];
            }
        }
        return 0;
    }

    /**
     * Every pickable command of a layer at a point, top-most first (each id once).
     *
     * @param out Receives the ids.
     * @return How many ids were written (at most {@code out.length}).
     */
    public int pickAll(Layer layer, float x, float y, int[] out) {
        PickSet set = pickSet(layer);
        int count = 0;
        for (int i = set.size - 1; i >= 0 && count < out.length; i--) {
            int id = set.ids[i];
            if (set.hit(i, x, y) && !contains(out, count, id)) {
                out[count++] = id;
            }
        }
        return count;
    }

    private static boolean contains(int[] ids, int count, int id) {
        for (int i = 0; i < count; i++) {
            if (ids[i] == id) {
                return true;
            }
        }
        return false;
    }

    private PickSet pickSet(Layer layer) {
        for (int i = 0; i < pickLayers.length; i++) {
            if (pickLayers[i] == layer) {
                return pickSets[i];
            }
        }
        throw new IllegalArgumentException("No pass of this pipeline draws " + layer + ".");
    }

    // endregion

    // region recording (by the pipeline)

    void begin(long frameNumber) {
        this.frameNumber = frameNumber;
        for (PickSet set : pickSets) {
            set.size = 0; // layers not drawn this frame have nothing to pick
        }
        for (PassStats pass : passes) {
            pass.drawCalls = 0;
            pass.nanos = 0;
        }
        for (LayerStats layer : layers) {
            layer.reset();
        }
        Arrays.fill(batchMaterial, 0, batchCount, null);
        batchCount = 0;
    }

    void end(long nanos) {
        this.nanos = nanos;
    }

    void addBatch(LayerStats layer, BatchBreak reason, Material material, int quads, int textures) {
        if (batchCount == batchQuads.length) {
            int capacity = batchCount * 2;
            batchLayer = Arrays.copyOf(batchLayer, capacity);
            batchReason = Arrays.copyOf(batchReason, capacity);
            batchMaterial = Arrays.copyOf(batchMaterial, capacity);
            batchQuads = Arrays.copyOf(batchQuads, capacity);
            batchTextures = Arrays.copyOf(batchTextures, capacity);
        }
        batchLayer[batchCount] = layer;
        batchReason[batchCount] = reason;
        batchMaterial[batchCount] = material;
        batchQuads[batchCount] = quads;
        batchTextures[batchCount] = textures;
        batchCount++;
        layer.batches++;
        layer.quads += quads;
        layer.breaks[reason.ordinal()]++;
    }

    /** Keep the pickable commands of a prepared layer, in drawing order. */
    void capturePicks(Layer layer, DrawList list) {
        PickSet set = pickSet(layer);
        set.size = 0;
        if (list.pickableCount() == 0) {
            return; // nothing pickable: no walk through the commands
        }
        for (int order = 0, n = list.size(); order < n; order++) {
            int index = list.orderedIndex(order);
            if (list.pickId(index) != 0) {
                set.add(list, index);
            }
        }
    }

    // endregion

    /**
     * The pickable commands of one layer: per command its corners, interpolated attributes, shape parameters, stroke
     * reach and clip, enough to hit-test it like the shader draws it.
     */
    private static final class PickSet {
        private static final int STRIDE = 37;
        // 0-7 corners (layer space), 8-15 local, 16-23 uv / shape dimensions, 24-27 params, 28 stroke reach,
        // 29 has clip, 30-33 clip rectangle, 34-36 dash length, gap, phase
        private float[] data = new float[STRIDE * 16];
        private int[] ids = new int[16];
        private byte[] types = new byte[16];
        int size;
        private final float[] attributes = new float[4]; // interpolated local x, y, u, v

        void add(DrawList list, int index) {
            int type = list.shapeType(index);
            if (list.layout(index) != org.pixel.graphics.backend.VertexLayout.FULL) {
                type = ShapeType.TEXTURED.code(); // sprite quads: their rectangle
            } else if (type == ShapeType.GLYPH.code() && list.param(index, 2) > 1.5f) {
                return; // a glyph's shadow layer: not where the text is
            }
            if (size == ids.length) {
                ids = Arrays.copyOf(ids, size * 2);
                types = Arrays.copyOf(types, size * 2);
                data = Arrays.copyOf(data, size * 2 * STRIDE);
            }
            int o = size * STRIDE;
            boolean full = list.layout(index) == org.pixel.graphics.backend.VertexLayout.FULL;
            for (int c = 0; c < 4; c++) {
                data[o + c * 2] = list.x(index, c);
                data[o + c * 2 + 1] = list.y(index, c);
                data[o + 8 + c * 2] = full ? list.localX(index, c) : 0f;
                data[o + 9 + c * 2] = full ? list.localY(index, c) : 0f;
                data[o + 16 + c * 2] = full ? list.cornerU(index, c) : 0f;
                data[o + 17 + c * 2] = full ? list.cornerV(index, c) : 0f;
            }
            for (int k = 0; k < 4; k++) {
                data[o + 24 + k] = full ? list.param(index, k) : 0f;
            }
            float reach = 0f;
            int style = full ? list.styleId(index) : 0;
            if (style != 0 && list.styles().get(style, StyleTable.STROKE_COLOR + 3) > 0f) {
                reach = Math.max(0f, list.styles().get(style, StyleTable.STROKE_OFFSET)
                        + 0.5f * list.styles().get(style, StyleTable.STROKE_WIDTH));
            }
            data[o + 28] = reach;
            data[o + 34] = style != 0 ? list.styles().get(style, StyleTable.DASH_LENGTH) : 0f;
            data[o + 35] = style != 0 ? list.styles().get(style, StyleTable.DASH_GAP) : 0f;
            data[o + 36] = style != 0 ? list.styles().get(style, StyleTable.DASH_PHASE) : 0f;
            int clip = full ? list.clipId(index) : 0;
            data[o + 29] = clip != 0 ? 1f : 0f;
            if (clip != 0) {
                data[o + 30] = list.clips().x0(clip);
                data[o + 31] = list.clips().y0(clip);
                data[o + 32] = list.clips().x1(clip);
                data[o + 33] = list.clips().y1(clip);
            }
            ids[size] = list.pickId(index);
            types[size] = (byte) type;
            size++;
        }

        boolean hit(int i, float x, float y) {
            int o = i * STRIDE;
            if (data[o + 29] > 0f && (x < data[o + 30] || y < data[o + 31] || x >= data[o + 32]
                    || y >= data[o + 33])) {
                return false;
            }
            // the two triangles the GPU draws: corners (0, 1, 2) and (2, 3, 0)
            if (!interpolate(o, 0, 1, 2, x, y) && !interpolate(o, 2, 3, 0, x, y)) {
                return false;
            }
            float lx = attributes[0], ly = attributes[1], u = attributes[2], v = attributes[3];
            int type = types[i];
            if (type == ShapeType.TEXTURED.code() || type == ShapeType.GLYPH.code()) {
                return true;
            } else if (type == ShapeType.PATH_FILL.code()) {
                return lx >= 0.5f; // interpolated coverage: inside the anti-aliased edge's middle
            } else if (type == ShapeType.PATH_STROKE.code()) {
                return Math.abs(ly) <= u; // distance across the line vs half its width
            }
            float reach = data[o + 28], dash = data[o + 34], gap = data[o + 35], phase = data[o + 36];
            if (type == ShapeType.RECT.code()) {
                return roundRect(lx, ly, u, v, o + 24) <= reach;
            } else if (type == ShapeType.ELLIPSE.code()) {
                float distance = ellipse(lx, ly, u, v);
                if (distance <= 0f) {
                    return true;
                }
                if (distance > reach) {
                    return false;
                }
                if (dash <= 0f) {
                    return true;
                }
                // the dashed stroke band: the pattern stretched to fit the outline, as the shader does
                float meanRadius = 0.5f * (u + v);
                float s = (float) (Math.atan2(ly, lx) + Math.PI) * meanRadius + phase;
                float circumference = (float) (2 * Math.PI) * meanRadius;
                float period = dash + gap;
                float fit = circumference / (Math.max(1f, Math.round(circumference / period)) * period);
                return inDash(s, dash * fit, gap * fit);
            }
            float distance = segment(lx, ly, u, v, data[o + 24]);
            if (distance > reach) {
                return false;
            }
            // a dashed line: its body is dashed (along its length, from the start cap), its stroke is not
            return dash <= 0f || distance > 0f || inDash(lx + u + phase, dash, gap);
        }

        private static boolean inDash(float s, float dash, float gap) {
            float period = dash + gap;
            float x = s - (float) Math.floor(s / period) * period;
            return x <= dash;
        }

        /** If (x, y) is inside triangle (a, b, c), interpolate its local coordinates and uv into attributes. */
        private boolean interpolate(int o, int a, int b, int c, float x, float y) {
            float ax = data[o + a * 2], ay = data[o + a * 2 + 1];
            float bx = data[o + b * 2], by = data[o + b * 2 + 1];
            float cx = data[o + c * 2], cy = data[o + c * 2 + 1];
            float area = (bx - ax) * (cy - ay) - (cx - ax) * (by - ay);
            if (Math.abs(area) < 1e-12f) {
                return false; // degenerate (path triangles repeat a corner)
            }
            float wb = ((x - ax) * (cy - ay) - (cx - ax) * (y - ay)) / area;
            float wc = ((bx - ax) * (y - ay) - (x - ax) * (by - ay)) / area;
            float wa = 1f - wb - wc;
            float epsilon = -1e-5f;
            if (wa < epsilon || wb < epsilon || wc < epsilon) {
                return false;
            }
            for (int k = 0; k < 2; k++) {
                attributes[k] = wa * data[o + 8 + a * 2 + k] + wb * data[o + 8 + b * 2 + k]
                        + wc * data[o + 8 + c * 2 + k];
                attributes[2 + k] = wa * data[o + 16 + a * 2 + k] + wb * data[o + 16 + b * 2 + k]
                        + wc * data[o + 16 + c * 2 + k];
            }
            return true;
        }

        // distance functions of the shape shader (negative inside)

        private float roundRect(float px, float py, float hx, float hy, int radii) {
            float r = px < 0f ? (py < 0f ? data[radii] : data[radii + 3]) : (py < 0f ? data[radii + 1]
                    : data[radii + 2]);
            r = Math.min(r, Math.min(hx, hy));
            float qx = Math.abs(px) - hx + r, qy = Math.abs(py) - hy + r;
            return Math.min(Math.max(qx, qy), 0f) + length(Math.max(qx, 0f), Math.max(qy, 0f)) - r;
        }

        private static float ellipse(float px, float py, float rx, float ry) {
            if (Math.abs(rx - ry) < 1e-4f) {
                return length(px, py) - rx;
            }
            float k0 = length(px / rx, py / ry);
            float k1 = length(px / (rx * rx), py / (ry * ry));
            return k1 < 1e-6f ? -Math.min(rx, ry) : k0 * (k0 - 1f) / k1;
        }

        private static float segment(float px, float py, float halfLength, float halfWidth, float cap) {
            if (cap > 0.5f && cap < 1.5f) {
                return length(Math.max(Math.abs(px) - halfLength, 0f), py) - halfWidth;
            }
            float bx = halfLength + (cap > 1.5f ? halfWidth : 0f);
            float qx = Math.abs(px) - bx, qy = Math.abs(py) - halfWidth;
            return length(Math.max(qx, 0f), Math.max(qy, 0f)) + Math.min(Math.max(qx, qy), 0f);
        }

        private static float length(float x, float y) {
            return (float) Math.sqrt(x * x + y * y);
        }
    }
}
