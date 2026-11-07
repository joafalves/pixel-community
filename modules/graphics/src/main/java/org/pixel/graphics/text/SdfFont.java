package org.pixel.graphics.text;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.content.Texture;

import java.util.Arrays;

/**
 * A multi-channel signed distance field font (MTSDF): one atlas texture plus glyph metrics and kerning. Text drawn
 * with it stays sharp at any size, zoom or rotation, and supports outlines, glows, shadows and weight changes
 * through {@code Canvas.text(...)}.
 * <p>
 * Loaded with {@code content().load("fonts/x.ttf", SdfFont.class, settings)} (generated at runtime, desktop) or
 * from a pre-baked msdf-atlas-gen atlas ({@code .json} next to a {@code .png}). The font owns its atlas texture.
 * <p>
 * Lookups are allocation-free. Metrics are in em: multiply by the font size in pixels.
 */
public class SdfFont implements Disposable {

    private static final int ASCII = 128;

    private final Texture atlas;
    private final boolean trueDistance;
    private final float distanceRange;
    private final float glyphSize;
    private final float ascender;
    private final float descender;
    private final float lineHeight;

    private final int[] codepoints;
    private final float[] advances;
    private final float[] planeBounds;
    private final float[] uvs;
    private final boolean[] visible;
    private final int[] asciiIndex = new int[ASCII];
    private final long[] kerningPairs;
    private final float[] kerningValues;
    private final int fallback;

    /**
     * Constructor.
     *
     * @param data  The font description.
     * @param atlas The atlas texture (owned by the font from now on). Its size must match the data.
     */
    public SdfFont(SdfFontData data, Texture atlas) {
        if (data == null || atlas == null) {
            throw new IllegalArgumentException("Font data and atlas cannot be null.");
        }
        if (atlas.getWidth() != data.getAtlasWidth() || atlas.getHeight() != data.getAtlasHeight()) {
            throw new IllegalArgumentException("Atlas size " + atlas.getWidth() + "x" + atlas.getHeight()
                    + " does not match the font data (" + data.getAtlasWidth() + "x" + data.getAtlasHeight() + ").");
        }
        data.finish();
        this.atlas = atlas;
        this.trueDistance = data.getType().equals("mtsdf");
        this.distanceRange = data.getDistanceRange();
        this.glyphSize = data.getGlyphSize();
        this.ascender = data.getAscender();
        this.descender = data.getDescender();
        this.lineHeight = data.getLineHeight() > 0f ? data.getLineHeight() : data.getAscender() + data.getDescender();

        int n = data.getGlyphCount();
        codepoints = Arrays.copyOf(data.codepoints(), n);
        advances = Arrays.copyOf(data.advances(), n);
        planeBounds = Arrays.copyOf(data.planeBounds(), n * 4);
        uvs = new float[n * 4];
        visible = new boolean[n];
        float[] box = data.atlasBounds();
        float w = data.getAtlasWidth(), h = data.getAtlasHeight();
        for (int i = 0; i < n; i++) {
            int b = i * 4;
            visible[i] = data.isVisible(i);
            uvs[b] = box[b] / w;
            uvs[b + 1] = box[b + 1] / h;
            uvs[b + 2] = box[b + 2] / w;
            uvs[b + 3] = box[b + 3] / h;
        }
        Arrays.fill(asciiIndex, -1);
        for (int i = 0; i < n && codepoints[i] < ASCII; i++) {
            if (codepoints[i] >= 0) {
                asciiIndex[codepoints[i]] = i;
            }
        }
        kerningPairs = Arrays.copyOf(data.kerningPairs(), data.getKerningCount());
        kerningValues = Arrays.copyOf(data.kerningValues(), data.getKerningCount());

        int replacement = search(0xFFFD);
        fallback = replacement >= 0 ? replacement : glyph('?');
    }

    // region glyphs

    /**
     * @param codepoint Unicode code point.
     * @return The glyph index, or -1 when the font has no glyph for it.
     */
    public int glyph(int codepoint) {
        if (codepoint >= 0 && codepoint < ASCII) {
            return asciiIndex[codepoint];
        }
        return search(codepoint);
    }

    /**
     * @param codepoint Unicode code point.
     * @return The glyph index, or the replacement glyph (U+FFFD, else '?') when missing, or -1 if neither exists.
     */
    public int glyphOrFallback(int codepoint) {
        int g = glyph(codepoint);
        return g >= 0 ? g : fallback;
    }

    private int search(int codepoint) {
        int i = Arrays.binarySearch(codepoints, codepoint);
        return i >= 0 ? i : -1;
    }

    public int glyphCount() {
        return codepoints.length;
    }

    public int codepoint(int glyph) {
        return codepoints[glyph];
    }

    /** Horizontal advance (em). */
    public float advance(int glyph) {
        return advances[glyph];
    }

    /** Whether the glyph has an image (false for spaces). */
    public boolean isVisible(int glyph) {
        return visible[glyph];
    }

    /** Quad left relative to the pen (em). */
    public float left(int glyph) {
        return planeBounds[glyph * 4];
    }

    /** Quad top relative to the baseline, y down (em). */
    public float top(int glyph) {
        return planeBounds[glyph * 4 + 1];
    }

    /** Quad right relative to the pen (em). */
    public float right(int glyph) {
        return planeBounds[glyph * 4 + 2];
    }

    /** Quad bottom relative to the baseline, y down (em). */
    public float bottom(int glyph) {
        return planeBounds[glyph * 4 + 3];
    }

    public float u0(int glyph) {
        return uvs[glyph * 4];
    }

    public float v0(int glyph) {
        return uvs[glyph * 4 + 1];
    }

    public float u1(int glyph) {
        return uvs[glyph * 4 + 2];
    }

    public float v1(int glyph) {
        return uvs[glyph * 4 + 3];
    }

    /**
     * Kerning between two code points (em), added to the advance of the first one.
     */
    public float kerning(int first, int second) {
        if (kerningPairs.length == 0) {
            return 0f;
        }
        int i = Arrays.binarySearch(kerningPairs, SdfFontData.pairKey(first, second));
        return i >= 0 ? kerningValues[i] : 0f;
    }

    // endregion

    // region metrics

    /** Distance from the baseline to the top of the tallest glyphs (em, positive). */
    public float getAscender() {
        return ascender;
    }

    /** Distance from the baseline to the bottom of the lowest glyphs (em, positive). */
    public float getDescender() {
        return descender;
    }

    /** Distance between consecutive baselines (em). */
    public float getLineHeight() {
        return lineHeight;
    }

    /** Distance range encoded in the atlas (atlas pixels, edge to edge). */
    public float getDistanceRange() {
        return distanceRange;
    }

    /** Atlas pixels per em. */
    public float getGlyphSize() {
        return glyphSize;
    }

    /**
     * Distance range in output units when drawn at {@code size}: outlines, glows and weight can reach up to half
     * of it outside the glyph edge.
     */
    public float distanceRange(float size) {
        return distanceRange * size / glyphSize;
    }

    /**
     * Whether the atlas alpha holds the true distance (MTSDF). Without it, outlines, glows and shadows use the
     * median distance, which is less accurate away from the edge.
     */
    public boolean hasTrueDistance() {
        return trueDistance;
    }

    public Texture getAtlas() {
        return atlas;
    }

    /**
     * Width of a single line of text at {@code size} (kerning included, no line breaks).
     */
    public float measure(CharSequence text, float size) {
        float width = 0f;
        int previous = -1;
        for (int i = 0; i < text.length(); ) {
            int cp = Character.codePointAt(text, i);
            i += Character.charCount(cp);
            int g = glyphOrFallback(cp);
            if (g < 0) {
                continue;
            }
            if (previous >= 0) {
                width += kerning(previous, cp) * size;
            }
            width += advances[g] * size;
            previous = cp;
        }
        return width;
    }

    // endregion

    @Override
    public void dispose() {
        atlas.dispose();
    }
}
