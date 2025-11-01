/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas.text;

/**
 * Represents a single glyph (character) in an SDF font.
 * Contains metrics for positioning and texture coordinates for rendering.
 */
public class SdfGlyph {
    
    private final char character;
    private final int atlasX;
    private final int atlasY;
    private final int width;
    private final int height;
    private final int offsetX;
    private final int offsetY;
    private final int advance;

    /**
     * Constructor.
     *
     * @param character The Unicode character
     * @param atlasX    X position in atlas texture (pixels)
     * @param atlasY    Y position in atlas texture (pixels)
     * @param width     Glyph width in atlas (pixels)
     * @param height    Glyph height in atlas (pixels)
     * @param offsetX   Horizontal bearing (pixels)
     * @param offsetY   Vertical bearing (pixels)
     * @param advance   Horizontal advance to next glyph (pixels)
     */
    public SdfGlyph(char character, int atlasX, int atlasY, int width, int height, 
                    int offsetX, int offsetY, int advance) {
        this.character = character;
        this.atlasX = atlasX;
        this.atlasY = atlasY;
        this.width = width;
        this.height = height;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.advance = advance;
    }

    public char getCharacter() {
        return character;
    }

    public int getAtlasX() {
        return atlasX;
    }

    public int getAtlasY() {
        return atlasY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int getAdvance() {
        return advance;
    }
}
