/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

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
     * Constructor.
     *
     * @param x        X position.
     * @param y        Y position.
     * @param width    Width.
     * @param height   Height.
     * @param xadvance Advance in X axis.
     * @param xoff     Offset in X axis.
     * @param yoff     Offset in Y axis.
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

    /**
     * Get the X position.
     *
     * @return X position.
     */
    public float getX() {
        return x;
    }

    /**
     * Get the Y position.
     *
     * @return Y position.
     */
    public float getY() {
        return y;
    }

    /**
     * Get the width.
     *
     * @return Width.
     */
    public float getWidth() {
        return width;
    }

    /**
     * Get the height.
     *
     * @return Height.
     */
    public float getHeight() {
        return height;
    }

    /**
     * Get the advance in X axis.
     *
     * @return Advance in X axis.
     */
    public float getXAdvance() {
        return xadvance;
    }

    /**
     * Get the offset in X axis.
     *
     * @return Offset in X axis.
     */
    public float getXOffset() {
        return xoff;
    }

    /**
     * Get the offset in Y axis.
     *
     * @return Offset in Y axis.
     */
    public float getYOffset() {
        return yoff;
    }

    //endregion
}
