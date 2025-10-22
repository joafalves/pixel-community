/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas.text;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.content.Texture;
import org.pixel.graphics.render.canvas.TextStyle;
import org.pixel.math.Size;
import org.pixel.math.Vector2;

import java.util.HashMap;
import java.util.Map;

/**
 * SDF (Signed Distance Field) font for high-quality text rendering.
 * Independent font system optimized for Canvas Renderer.
 *
 * <p>SDF fonts provide:
 * <ul>
 *   <li>Crisp rendering at any scale</li>
 *   <li>Smooth anti-aliasing</li>
 *   <li>Stroke/outline effects</li>
 *   <li>Glow and shadow effects</li>
 * </ul>
 */
public class SdfFont implements Disposable {

    /**
     * Space width ratio relative to font size.
     * This matches the rendering behavior in GLSdfTextRenderer.
     */
    private static final float SPACE_WIDTH_RATIO = 0.25f;

    private final Texture atlasTexture;
    private final Map<Character, SdfGlyph> glyphs;
    private final int fontSize;
    private final int lineHeight;
    private final int atlasWidth;
    private final int atlasHeight;
    private final int ascent;  // Font ascent in pixels (baseline to top)

    /**
     * Constructor.
     *
     * @param atlasTexture The SDF texture atlas
     * @param fontSize     The base font size used for generation
     * @param lineHeight   The line height (spacing between lines)
     * @param atlasWidth   Atlas texture width
     * @param atlasHeight  Atlas texture height
     * @param ascent       The font ascent (baseline to top) in pixels
     */
    public SdfFont(Texture atlasTexture, int fontSize, int lineHeight, int atlasWidth, int atlasHeight, int ascent) {
        this.atlasTexture = atlasTexture;
        this.fontSize = fontSize;
        this.lineHeight = lineHeight;
        this.atlasWidth = atlasWidth;
        this.atlasHeight = atlasHeight;
        this.ascent = ascent;
        this.glyphs = new HashMap<>();
    }

    /**
     * Add a glyph to this font.
     */
    public void addGlyph(SdfGlyph glyph) {
        glyphs.put(glyph.getCharacter(), glyph);
    }

    /**
     * Get SDF-specific glyph data for a character.
     *
     * @return The SDF glyph, or null if character not in font
     */
    public SdfGlyph getGlyph(char character) {
        return glyphs.get(character);
    }

    /**
     * Check if this font contains a character.
     */
    public boolean hasGlyph(char character) {
        return glyphs.containsKey(character);
    }

    public Texture getAtlasTexture() {
        return atlasTexture;
    }

    public int getFontSize() {
        return fontSize;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public int getAtlasWidth() {
        return atlasWidth;
    }

    public int getAtlasHeight() {
        return atlasHeight;
    }

    public int getAscent() {
        return ascent;
    }

    /**
     * Measure text dimensions with default spacing and no scale.
     *
     * @param text The text to measure
     * @return The size of the text when rendered
     */
    public Size measureText(String text) {
        return measureText(text, 0, 0, new Vector2(1, 1));
    }
    
    /**
     * Measure text dimensions with uniform scale (zero-GC version).
     * 
     * <p>This method is used by Canvas/Renderer to measure text with a combined
     * scale factor (fontSize scale * transform scale). It delegates to the full
     * measurement method with uniform X/Y scale.
     *
     * @param text  The text to measure
     * @param scale Uniform scale factor to apply (combines fontSize and transform scales)
     * @return The size of the text when rendered
     */
    public Size measureText(String text, float scale) {
        return measureText(text, 0, 0, scale, scale);
    }

    /**
     * Measure text dimensions with custom style and no scale.
     *
     * @param text  The text to measure
     * @param style Text style containing letter and line spacing
     * @return The size of the text when rendered
     */
    public Size measureText(String text, TextStyle style) {
        float letterSpacing = style != null ? style.getLetterSpacing() : 0;
        float lineSpacing = style != null ? style.getLineSpacing() : 0;
        return measureText(text, letterSpacing, lineSpacing, new Vector2(1, 1));
    }

    /**
     * Measure text dimensions with custom style and scale.
     *
     * @param text  The text to measure
     * @param style Text style containing letter and line spacing
     * @param scale Scale factor to apply to measurements
     * @return The size of the text when rendered
     */
    public Size measureText(String text, TextStyle style, Vector2 scale) {
        float letterSpacing = style != null ? style.getLetterSpacing() : 0;
        float lineSpacing = style != null ? style.getLineSpacing() : 0;
        return measureText(text, letterSpacing, lineSpacing, scale);
    }

    /**
     * Measure text dimensions with custom letter spacing, line spacing, and scale.
     *
     * <p>This is the most flexible measurement method. It calculates the exact dimensions
     * of the rendered text by iterating through all characters and summing their advances,
     * accounting for letter spacing, line spacing, and scale transformations.
     *
     * <p>The measurement logic mirrors the rendering logic in GLSdfTextRenderer to ensure
     * accurate results.
     *
     * @param text          The text to measure
     * @param letterSpacing Additional spacing between characters (in pixels, before scale)
     * @param lineSpacing   Additional spacing between lines (in pixels, before scale)
     * @param scale         Scale factor to apply (x, y)
     * @return The size of the text when rendered
     */
    public Size measureText(String text, float letterSpacing, float lineSpacing, Vector2 scale) {
        return measureText(text, letterSpacing, lineSpacing, scale.getX(), scale.getY());
    }

    /**
     * Measure text dimensions with custom letter spacing, line spacing, and scale.
     *
     * <p>This is the most flexible measurement method. It calculates the exact dimensions
     * of the rendered text by iterating through all characters and summing their advances,
     * accounting for letter spacing, line spacing, and scale transformations.
     *
     * <p>The measurement logic mirrors the rendering logic in GLSdfTextRenderer to ensure
     * accurate results.
     *
     * @param text          The text to measure
     * @param letterSpacing Additional spacing between characters (in pixels, before scale)
     * @param lineSpacing   Additional spacing between lines (in pixels, before scale)
     * @param scaleX        Scale factor to apply on X axis
     * @param scaleY        Scale factor to apply on Y axis
     * @return The size of the text when rendered
     */
    public Size measureText(String text, float letterSpacing, float lineSpacing, float scaleX, float scaleY) {
        if (text == null || text.isEmpty()) {
            return new Size(0, 0);
        }

        String[] lines = text.split("\n", -1);
        float maxWidth = 0;
        float totalHeight = 0;

        for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
            String line = lines[lineIdx];
            float lineWidth = 0;

            for (int i = 0; i < line.length(); i++) {
                char ch = line.charAt(i);

                // Handle spaces (matching the rendering logic in GLSdfTextRenderer)
                if (ch == ' ') {
                    float spaceWidth = fontSize * SPACE_WIDTH_RATIO * scaleX;
                    lineWidth += spaceWidth + (letterSpacing * scaleX);
                    continue;
                }

                SdfGlyph glyph = getGlyph(ch);
                if (glyph != null) {
                    lineWidth += (glyph.getAdvance() * scaleX) + (letterSpacing * scaleX);
                }
            }

            maxWidth = Math.max(maxWidth, lineWidth);
            totalHeight += lineHeight * scaleY;

            if (lineIdx < lines.length - 1) {
                totalHeight += lineSpacing * scaleY;
            }
        }

        // Return final size (already scaled during accumulation)
        return new Size(maxWidth, totalHeight);
    }

    @Override
    public void dispose() {
        if (atlasTexture != null) {
            atlasTexture.dispose();
        }
    }
}
