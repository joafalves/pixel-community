/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import java.util.Map;

import org.pixel.commons.lifecycle.Disposable;

public abstract class Font implements Disposable {

    //region Fields & Properties

    protected final int fontSize;
    protected final Map<Character, FontGlyph> glyphs;

    private int horizontalSpacing = 0;
    private int verticalSpacing = 0;

    //endregion

    //region Public Functions

    public Font(int fontSize, Map<Character, FontGlyph> glyphs) {
        this.fontSize = fontSize;
        this.glyphs = glyphs;
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
     * Compute the width (in pixels) of a given string.
     *
     * @param text The text to compute the size of.
     * @return The width of the given text.
     */
    public int measure(String text) {
        return measure(text, fontSize);
    }

    /**
     * Compute the width (in pixels) of a given string with a given font size.
     *
     * @param text     The text to compute the size of.
     * @param fontSize The font size to use.
     * @return The width of the given text.
     */
    public int measure(String text, float fontSize) {
        int width = 0;
        float scale = fontSize / (float) getFontSize();
        for (char ch : text.toCharArray()) {
            FontGlyph glyph = getGlyph(ch);
            if (glyph == null) {
                continue; // cannot process this char data...
            }

            width += (int) (glyph.getXAdvance() * scale + getHorizontalSpacing());
        }

        return width;
    }

    /**
     * Get glyph data for a given character.
     *
     * @param ch The character to get glyph data for.
     * @return The glyph data for the given character.
     */
    public FontGlyph getGlyph(char ch) {
        return this.glyphs.get(ch);
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

    //endregion
}
