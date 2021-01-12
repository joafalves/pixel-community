/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

/**
 * @author João Filipe Alves
 */
public class FontGlyph {
    //region Fields & Properties

    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final float xadvance;
    private final float xoff;
    private final float yoff;

    //endregion

    //region Constructors

    /**
     * Constructor
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param xadvance
     * @param xoff
     * @param yoff
     */
    public FontGlyph(float x, float y, float width, float height, float xadvance, float xoff, float yoff) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.xadvance = xadvance;
        this.xoff = xoff;
        this.yoff = yoff;
    }

    //endregion

    //region Public Functions

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getXAdvance() {
        return xadvance;
    }

    public float getXOffset() {
        return xoff;
    }

    public float getYOffset() {
        return yoff;
    }

    //endregion
}
