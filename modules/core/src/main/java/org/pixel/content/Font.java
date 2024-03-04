/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import static org.lwjgl.opengl.GL11C.glDeleteTextures;
import static org.lwjgl.stb.STBImageWrite.stbi_write_bmp;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import org.lwjgl.stb.STBTTPackedchar;
import org.pixel.commons.lifecycle.Disposable;

public abstract class Font implements Disposable {

    //region Fields & Properties

    protected static final int GLYPH_TEXTURE_PADDING = 1;
    protected static final int GLYPH_TEXTURE_PADDING_COMPENSATION = 4;

    protected final FontData fontData;
    protected int textureId;
    protected int textureWidth;
    protected int textureHeight;
    protected int fontSize;
    protected int horizontalSpacing;
    protected int verticalSpacing;
    protected int oversampling;
    protected STBTTPackedchar.Buffer packedBuffer;
    protected ByteBuffer bitmap;
    protected ConcurrentHashMap<Character, FontGlyph> glyphCache;

    //endregion

    //region Constructors

    /**
     * Constructor.
     *
     * @param fontData The font data.
     */
    public Font(FontData fontData) {
        this.fontData = fontData;
        this.fontSize = 24;
        this.horizontalSpacing = 0;
        this.verticalSpacing = 0;
        this.oversampling = 1;
        this.init();
    }

    /**
     * Constructor.
     *
     * @param fontData          The font data.
     * @param fontSize          The initial font size.
     * @param horizontalSpacing The horizontal spacing.
     * @param verticalSpacing   The vertical spacing.
     * @param oversampling      The oversampling.
     */
    public Font(FontData fontData, int fontSize, int horizontalSpacing, int verticalSpacing, int oversampling) {
        this.fontData = fontData;
        this.fontSize = fontSize;
        this.horizontalSpacing = horizontalSpacing;
        this.verticalSpacing = verticalSpacing;
        this.oversampling = oversampling;
        this.init();
    }

    //endregion

    //region Private Functions

    private void init() {
        this.textureId = -1;
        this.packedBuffer = STBTTPackedchar.malloc(6 * 128);
        this.glyphCache = new ConcurrentHashMap<>();
        this.computeFontData();
    }

    /**
     * Generate and compute font data based on the current properties
     */
    protected abstract void computeFontData();

    //endregion

    //region Public Functions

    /**
     * Save font texture as PNG to a given path.
     *
     * @param filepath The path to save the PNG to.
     */
    public void saveAsPng(String filepath) {
        stbi_write_png(filepath, this.textureWidth, this.textureHeight, 4, bitmap, 0);
    }

    /**
     * Save font texture as BMP to a given path.
     *
     * @param filepath The path to save the BMP to.
     */
    public void saveAsBmp(String filepath) {
        stbi_write_bmp(filepath, this.textureWidth, this.textureHeight, 4, bitmap);
    }

    /**
     * Get font size.
     *
     * @return The font size.
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Get the computed font texture width (font size * oversampling).
     *
     * @return The font size.
     */
    public int getComputedFontSize() {
        return fontSize * oversampling;
    }

    /**
     * Set font size. This call triggers a font data recompute to adjust the source texture.
     *
     * @param fontSize The font size.
     */
    public void setFontSize(int fontSize) {
        if (fontSize != this.fontSize) {
            this.fontSize = fontSize;
            this.computeFontData();
        }
    }

    /**
     * Compute the width (in pixels) of a given string.
     *
     * @param text The text to compute the size of.
     * @return The width of the given text.
     */
    public int computeTextWidth(String text) {
        return computeTextWidth(text, fontSize);
    }

    /**
     * Compute the width (in pixels) of a given string with a given font size.
     *
     * @param text     The text to compute the size of.
     * @param fontSize The font size to use.
     * @return The width of the given text.
     */
    public int computeTextWidth(String text, float fontSize) {
        int width = 0;
        float scale = fontSize / (float) getFontSize();
        for (char ch : text.toCharArray()) {
            FontGlyph glyph = getGlyph(ch);
            if (glyph == null) {
                continue; // cannot process this char data...
            }

            width += glyph.getXAdvance() * scale + getHorizontalSpacing();
        }

        return width;
    }

    /**
     * Get font native texture id.
     *
     * @return The font native texture id.
     */
    public int getTextureId() {
        return textureId;
    }

    /**
     * Get the font texture width.
     *
     * @return The texture width.
     */
    public int getTextureWidth() {
        return this.textureWidth;
    }

    /**
     * Get the font texture height.
     *
     * @return The texture height.
     */
    public int getTextureHeight() {
        return this.textureHeight;
    }

    /**
     * Get glyph data for a given character.
     *
     * @param ch The character to get glyph data for.
     * @return The glyph data for the given character.
     */
    public FontGlyph getGlyph(char ch) {
        FontGlyph glyph = this.glyphCache.get(ch);
        if (glyph == null) {
            STBTTPackedchar pc = this.packedBuffer.get(ch);
            if (ch == 258) {
                // tab
                return null; // TODO: add tab spacing
            }

            // create a new font glyph
            glyph = new FontGlyph(pc.x0(), pc.y0(), pc.x1() - pc.x0(), pc.y1() - pc.y0(), pc.xadvance(),
                    pc.xoff(), pc.yoff());

            this.glyphCache.put(ch, glyph);
        }

        return glyph;
    }

    /**
     * Get the horizontal spacing between characters.
     *
     * @return The horizontal spacing between characters.
     */
    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    /**
     * Set the horizontal spacing between characters.
     *
     * @param horizontalSpacing The horizontal spacing between characters.
     */
    public void setHorizontalSpacing(int horizontalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
    }

    /**
     * Get the vertical spacing between lines.
     *
     * @return The vertical spacing between lines.
     */
    public int getVerticalSpacing() {
        return verticalSpacing;
    }

    /**
     * Set the vertical spacing between lines.
     *
     * @param verticalSpacing The vertical spacing between lines.
     */
    public void setVerticalSpacing(int verticalSpacing) {
        this.verticalSpacing = verticalSpacing;
    }

    /**
     * Get the font oversampling.
     *
     * @return The font oversampling.
     */
    public int getOversampling() {
        return oversampling;
    }

    /**
     * Set the font oversampling.
     *
     * @param oversampling The font oversampling.
     */
    public void setOversampling(int oversampling) {
        if (oversampling != this.oversampling) {
            this.oversampling = oversampling;
            this.computeFontData();
        }
    }

    /**
     * Get the font data.
     *
     * @return The font data.
     */
    public FontData getFontData() {
        return fontData;
    }

    /**
     * Dispose of the font.
     */
    @Override
    public void dispose() {
        if (this.textureId >= 0) {
            glDeleteTextures(this.textureId);
        }
    }

    //endregion
}
