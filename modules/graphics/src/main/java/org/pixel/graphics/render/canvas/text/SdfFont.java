/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas.text;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.content.Texture;

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

    @Override
    public void dispose() {
        if (atlasTexture != null) {
            atlasTexture.dispose();
        }
    }
}
