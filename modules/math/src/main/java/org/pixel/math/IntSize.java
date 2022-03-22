/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.io.Serializable;
import lombok.Builder;

public class IntSize implements Serializable {

    private int width;
    private int height;

    /**
     * Constructor.
     *
     * @param width  The width of the size.
     * @param height The height of the size.
     */
    @Builder
    public IntSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Constructor.
     *
     * @param other The other size.
     */
    public IntSize(IntSize other) {
        this.width = other.width;
        this.height = other.height;
    }

    /**
     * Set the size to the given values.
     *
     * @param width  The width of the size.
     * @param height The height of the size.
     */
    public void set(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Get the height of the size.
     *
     * @return The height of the size.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the area of the size.
     *
     * @param height The height of the size.
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Get the width of the size.
     *
     * @return The width of the size.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get half the width of the size.
     *
     * @return The half the width of the size.
     */
    public int getHalfWidth() {
        return width / 2;
    }

    /**
     * Get half the height of the size.
     *
     * @return The half the height of the size.
     */
    public int getHalfHeight() {
        return height / 2;
    }

    /**
     * Set the width of the size.
     *
     * @param width The width of the size.
     */
    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntSize) {
            return ((IntSize) obj).getWidth() == this.getWidth() && ((IntSize) obj).getHeight() == this.getHeight();
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s: [w: %d, h: %d]", this.getClass().getSimpleName(), getWidth(), getHeight());
    }
}
