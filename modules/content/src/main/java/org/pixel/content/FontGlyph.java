/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
public class FontGlyph {
    //region Fields & Properties

    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final float xAdvance;
    private final float xOffset;
    private final float yOffset;

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
        return xAdvance;
    }

    /**
     * Get the offset in X axis.
     *
     * @return Offset in X axis.
     */
    public float getXOffset() {
        return xOffset;
    }

    /**
     * Get the offset in Y axis.
     *
     * @return Offset in Y axis.
     */
    public float getYOffset() {
        return yOffset;
    }

    //endregion
}
