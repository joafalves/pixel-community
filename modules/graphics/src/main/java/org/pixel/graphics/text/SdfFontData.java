package org.pixel.graphics.text;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;

/**
 * GL-free description of a distance-field font: atlas properties, font metrics, glyphs and kerning, and optionally
 * the atlas pixels (RGBA8, rows top to bottom). Produced by the runtime generator or parsed from a pre-baked
 * <a href="https://github.com/Chlumsky/msdf-atlas-gen">msdf-atlas-gen</a> JSON file, and turned into an
 * {@link SdfFont} once the atlas is uploaded.
 * <p>
 * Units: metrics, advances, plane bounds and kerning are in <b>em</b> (1 = the font size), with the y axis pointing
 * down and the origin on the baseline at the pen position. Atlas bounds are in atlas pixels, y axis down.
 */
public final class SdfFontData {

    private String type = "mtsdf";
    private float distanceRange;
    private float glyphSize;
    private int atlasWidth;
    private int atlasHeight;
    private float ascender;
    private float descender;
    private float lineHeight;

    private int glyphCount;
    private int[] codepoints = new int[128];
    private float[] advances = new float[128];
    private float[] planeBounds = new float[128 * 4]; // left, top, right, bottom (em, y down); all 0 = invisible
    private float[] atlasBounds = new float[128 * 4]; // left, top, right, bottom (atlas pixels, y down)

    private int kerningCount;
    private long[] kerningPairs = new long[64];
    private float[] kerningValues = new float[64];

    private ByteBuffer pixels;
    private boolean sorted = true;

    /**
     * Constructor.
     *
     * @param distanceRange Distance range encoded in the atlas (atlas pixels, edge to edge).
     * @param glyphSize     Atlas pixels per em.
     * @param atlasWidth    Atlas width (pixels).
     * @param atlasHeight   Atlas height (pixels).
     */
    public SdfFontData(float distanceRange, float glyphSize, int atlasWidth, int atlasHeight) {
        if (distanceRange <= 0f || glyphSize <= 0f || atlasWidth <= 0 || atlasHeight <= 0) {
            throw new IllegalArgumentException("Distance range, glyph size and atlas size must be positive.");
        }
        this.distanceRange = distanceRange;
        this.glyphSize = glyphSize;
        this.atlasWidth = atlasWidth;
        this.atlasHeight = atlasHeight;
    }

    // region building

    /**
     * Set the vertical metrics (em).
     *
     * @param ascender   Distance from the baseline to the top of the tallest glyphs (positive).
     * @param descender  Distance from the baseline to the bottom of the lowest glyphs (positive).
     * @param lineHeight Distance between consecutive baselines.
     */
    public SdfFontData metrics(float ascender, float descender, float lineHeight) {
        this.ascender = ascender;
        this.descender = descender;
        this.lineHeight = lineHeight;
        return this;
    }

    /**
     * Add a glyph without an image (space and other blank characters).
     */
    public SdfFontData addGlyph(int codepoint, float advance) {
        return addGlyph(codepoint, advance, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
    }

    /**
     * Add a glyph.
     *
     * @param codepoint Unicode code point.
     * @param advance   Horizontal advance (em).
     * @param left      Quad left, relative to the pen (em).
     * @param top       Quad top, relative to the baseline, y down (em; negative above the baseline).
     * @param right     Quad right (em).
     * @param bottom    Quad bottom (em).
     * @param atlasLeft   Atlas rectangle left (pixels).
     * @param atlasTop    Atlas rectangle top (pixels, y down).
     * @param atlasRight  Atlas rectangle right (pixels).
     * @param atlasBottom Atlas rectangle bottom (pixels).
     */
    public SdfFontData addGlyph(int codepoint, float advance, float left, float top, float right, float bottom,
                                float atlasLeft, float atlasTop, float atlasRight, float atlasBottom) {
        if (glyphCount == codepoints.length) {
            int capacity = glyphCount * 2;
            codepoints = Arrays.copyOf(codepoints, capacity);
            advances = Arrays.copyOf(advances, capacity);
            planeBounds = Arrays.copyOf(planeBounds, capacity * 4);
            atlasBounds = Arrays.copyOf(atlasBounds, capacity * 4);
        }
        if (glyphCount > 0 && codepoint <= codepoints[glyphCount - 1]) {
            sorted = false;
        }
        int i = glyphCount++;
        codepoints[i] = codepoint;
        advances[i] = advance;
        int b = i * 4;
        planeBounds[b] = left;
        planeBounds[b + 1] = top;
        planeBounds[b + 2] = right;
        planeBounds[b + 3] = bottom;
        atlasBounds[b] = atlasLeft;
        atlasBounds[b + 1] = atlasTop;
        atlasBounds[b + 2] = atlasRight;
        atlasBounds[b + 3] = atlasBottom;
        return this;
    }

    /**
     * Add a kerning adjustment (em, added to the advance of {@code first} when followed by {@code second}).
     */
    public SdfFontData addKerning(int first, int second, float advance) {
        if (kerningCount == kerningPairs.length) {
            kerningPairs = Arrays.copyOf(kerningPairs, kerningCount * 2);
            kerningValues = Arrays.copyOf(kerningValues, kerningCount * 2);
        }
        long key = pairKey(first, second);
        if (kerningCount > 0 && key <= kerningPairs[kerningCount - 1]) {
            sorted = false;
        }
        kerningPairs[kerningCount] = key;
        kerningValues[kerningCount] = advance;
        kerningCount++;
        return this;
    }

    /**
     * Set the atlas pixels: RGBA8, {@code atlasWidth * atlasHeight * 4} bytes, rows from top to bottom.
     */
    public SdfFontData pixels(ByteBuffer pixels) {
        if (pixels != null && pixels.remaining() != atlasWidth * atlasHeight * 4) {
            throw new IllegalArgumentException("Expected " + atlasWidth * atlasHeight * 4 + " bytes of RGBA pixels.");
        }
        this.pixels = pixels;
        return this;
    }

    /**
     * Sort glyphs and kerning pairs (lookups use binary search); duplicated glyphs are rejected, a kerning pair given
     * twice keeps its last value. Called by {@link SdfFont}.
     */
    void finish() {
        if (sorted) {
            return;
        }
        Integer[] order = new Integer[glyphCount];
        for (int i = 0; i < glyphCount; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (a, b) -> Integer.compare(codepoints[a], codepoints[b]));
        int[] cps = new int[glyphCount];
        float[] adv = new float[glyphCount];
        float[] plane = new float[glyphCount * 4];
        float[] atlas = new float[glyphCount * 4];
        for (int i = 0; i < glyphCount; i++) {
            int from = order[i];
            cps[i] = codepoints[from];
            if (i > 0 && cps[i] == cps[i - 1]) {
                throw new IllegalStateException("Duplicated glyph: U+" + Integer.toHexString(cps[i]));
            }
            adv[i] = advances[from];
            System.arraycopy(planeBounds, from * 4, plane, i * 4, 4);
            System.arraycopy(atlasBounds, from * 4, atlas, i * 4, 4);
        }
        codepoints = cps;
        advances = adv;
        planeBounds = plane;
        atlasBounds = atlas;

        Integer[] kOrder = new Integer[kerningCount];
        for (int i = 0; i < kerningCount; i++) {
            kOrder[i] = i;
        }
        Arrays.sort(kOrder, (a, b) -> Long.compare(kerningPairs[a], kerningPairs[b]));
        long[] keys = new long[kerningCount];
        float[] values = new float[kerningCount];
        int unique = 0;
        for (int i = 0; i < kerningCount; i++) {
            long pair = kerningPairs[kOrder[i]];
            if (unique > 0 && keys[unique - 1] == pair) {
                values[unique - 1] = kerningValues[kOrder[i]]; // a pair given twice: the last one wins
                continue;
            }
            keys[unique] = pair;
            values[unique] = kerningValues[kOrder[i]];
            unique++;
        }
        kerningPairs = Arrays.copyOf(keys, unique);
        kerningValues = Arrays.copyOf(values, unique);
        kerningCount = unique;
        sorted = true;
    }

    static long pairKey(int first, int second) {
        return ((long) first << 32) | (second & 0xFFFFFFFFL);
    }

    // endregion

    // region accessors

    public String getType() {
        return type;
    }

    public float getDistanceRange() {
        return distanceRange;
    }

    public float getGlyphSize() {
        return glyphSize;
    }

    public int getAtlasWidth() {
        return atlasWidth;
    }

    public int getAtlasHeight() {
        return atlasHeight;
    }

    public float getAscender() {
        return ascender;
    }

    public float getDescender() {
        return descender;
    }

    public float getLineHeight() {
        return lineHeight;
    }

    public int getGlyphCount() {
        return glyphCount;
    }

    public int getKerningCount() {
        return kerningCount;
    }

    /**
     * @return The atlas pixels (RGBA8, rows top to bottom), or {@code null} when they live elsewhere (an image file).
     */
    public ByteBuffer getPixels() {
        return pixels;
    }

    int[] codepoints() {
        return codepoints;
    }

    float[] advances() {
        return advances;
    }

    float[] planeBounds() {
        return planeBounds;
    }

    float[] atlasBounds() {
        return atlasBounds;
    }

    long[] kerningPairs() {
        return kerningPairs;
    }

    float[] kerningValues() {
        return kerningValues;
    }

    // endregion

    // region msdf-atlas-gen JSON

    /**
     * Parse the JSON layout written by msdf-atlas-gen ({@code -json}) or by {@link #toJson()}. Supports both y
     * origins and any {@code emSize}; the atlas image is loaded separately.
     *
     * @param json The JSON text.
     * @return The font data (without pixels).
     */
    public static SdfFontData fromJson(String json) {
        JSONObject root = new JSONObject(json);
        JSONObject atlas = root.getJSONObject("atlas");
        String type = atlas.optString("type", "mtsdf");
        if (!type.equals("mtsdf") && !type.equals("msdf") && !type.equals("sdf") && !type.equals("psdf")) {
            throw new IllegalArgumentException("Unsupported atlas type: " + type);
        }
        int width = atlas.getInt("width");
        int height = atlas.getInt("height");
        boolean yBottom = !"top".equals(atlas.optString("yOrigin", "bottom"));
        SdfFontData data = new SdfFontData((float) atlas.getDouble("distanceRange"), (float) atlas.getDouble("size"),
                width, height);
        data.type = type;

        JSONObject metrics = root.getJSONObject("metrics");
        float em = (float) metrics.optDouble("emSize", 1.0);
        // msdf-atlas-gen writes y-up metrics (ascender > 0, descender < 0) for yOrigin bottom, y-down otherwise
        float ascender = (float) metrics.getDouble("ascender") / em;
        float descender = (float) metrics.getDouble("descender") / em;
        if (yBottom) {
            data.metrics(ascender, -descender, (float) metrics.getDouble("lineHeight") / em);
        } else {
            data.metrics(-ascender, descender, (float) metrics.getDouble("lineHeight") / em);
        }

        JSONArray glyphs = root.getJSONArray("glyphs");
        for (int i = 0; i < glyphs.length(); i++) {
            JSONObject g = glyphs.getJSONObject(i);
            if (!g.has("unicode")) {
                continue; // glyph-index based atlases are not supported (no code point to map)
            }
            int cp = g.getInt("unicode");
            float advance = (float) g.getDouble("advance") / em;
            JSONObject plane = g.optJSONObject("planeBounds");
            JSONObject box = g.optJSONObject("atlasBounds");
            if (plane == null || box == null) {
                data.addGlyph(cp, advance);
                continue;
            }
            float l = (float) plane.getDouble("left") / em, r = (float) plane.getDouble("right") / em;
            float pb = (float) plane.getDouble("bottom") / em, pt = (float) plane.getDouble("top") / em;
            float al = (float) box.getDouble("left"), ar = (float) box.getDouble("right");
            float ab = (float) box.getDouble("bottom"), at = (float) box.getDouble("top");
            if (yBottom) { // y up: flip both
                data.addGlyph(cp, advance, l, -pt, r, -pb, al, height - at, ar, height - ab);
            } else {
                data.addGlyph(cp, advance, l, pt, r, pb, al, at, ar, ab);
            }
        }

        JSONArray kerning = root.optJSONArray("kerning");
        if (kerning != null) {
            for (int i = 0; i < kerning.length(); i++) {
                JSONObject k = kerning.getJSONObject(i);
                if (k.has("unicode1") && k.has("unicode2")) {
                    data.addKerning(k.getInt("unicode1"), k.getInt("unicode2"), (float) k.getDouble("advance") / em);
                }
            }
        }
        data.finish();
        return data;
    }

    /**
     * Write the msdf-atlas-gen JSON layout ({@code yOrigin: top}, {@code emSize: 1}), so atlases generated at
     * runtime can be shipped pre-baked (for example to mobile) and loaded with {@link #fromJson(String)}.
     */
    public String toJson() {
        finish();
        StringBuilder sb = new StringBuilder(4096 + glyphCount * 160 + kerningCount * 60);
        sb.append("{\"atlas\":{\"type\":\"").append(type).append('"')
                .append(",\"distanceRange\":").append(num(distanceRange))
                .append(",\"size\":").append(num(glyphSize))
                .append(",\"width\":").append(atlasWidth)
                .append(",\"height\":").append(atlasHeight)
                .append(",\"yOrigin\":\"top\"},\n");
        sb.append("\"metrics\":{\"emSize\":1,\"lineHeight\":").append(num(lineHeight))
                .append(",\"ascender\":").append(num(-ascender))
                .append(",\"descender\":").append(num(descender)).append("},\n\"glyphs\":[");
        for (int i = 0; i < glyphCount; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("\n{\"unicode\":").append(codepoints[i]).append(",\"advance\":").append(num(advances[i]));
            int b = i * 4;
            if (isVisible(i)) {
                sb.append(",\"planeBounds\":{\"left\":").append(num(planeBounds[b]))
                        .append(",\"top\":").append(num(planeBounds[b + 1]))
                        .append(",\"right\":").append(num(planeBounds[b + 2]))
                        .append(",\"bottom\":").append(num(planeBounds[b + 3])).append('}')
                        .append(",\"atlasBounds\":{\"left\":").append(num(atlasBounds[b]))
                        .append(",\"top\":").append(num(atlasBounds[b + 1]))
                        .append(",\"right\":").append(num(atlasBounds[b + 2]))
                        .append(",\"bottom\":").append(num(atlasBounds[b + 3])).append('}');
            }
            sb.append('}');
        }
        sb.append("],\n\"kerning\":[");
        for (int i = 0; i < kerningCount; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("\n{\"unicode1\":").append((int) (kerningPairs[i] >> 32))
                    .append(",\"unicode2\":").append((int) kerningPairs[i])
                    .append(",\"advance\":").append(num(kerningValues[i])).append('}');
        }
        sb.append("]}\n");
        return sb.toString();
    }

    boolean isVisible(int glyph) {
        int b = glyph * 4;
        return planeBounds[b + 2] > planeBounds[b] && planeBounds[b + 3] > planeBounds[b + 1];
    }

    private static String num(float v) {
        if (v == (int) v) {
            return Integer.toString((int) v);
        }
        return String.format(Locale.ROOT, "%.6g", v);
    }

    // endregion
}
