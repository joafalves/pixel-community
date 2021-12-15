/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.io.Serializable;

public class Size implements Serializable {

    private float width;
    private float height;

    /**
     * Constructor.
     */
    public Size() {
        this.width = 0.f;
        this.height = 0.f;
    }

    /**
     * Constructor.
     *
     * @param width  The width of the size.
     * @param height The height of the size.
     */
    public Size(float width, float height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Constructor.
     *
     * @param other The other size.
     */
    public Size(Size other) {
        this.width = other.width;
        this.height = other.height;
    }

    /**
     * Increment the width by the given value.
     *
     * @param value The value to increment by.
     */
    public void addWidth(float value) {
        this.width = width + value;
    }

    /**
     * Subtract the width by the given value.
     *
     * @param value The value to subtract by.
     */
    public void subtractWidth(float value) {
        this.width = width - value;
    }

    /**
     * Increment the height by the given value.
     *
     * @param value The value to increment by.
     */
    public void addHeight(float value) {
        this.height = height + value;
    }

    /**
     * Subtract the height by the given value.
     *
     * @param value The value to subtract by.
     */
    public void subtractHeight(float value) {
        this.height = height - value;
    }

    /**
     * Returns the height of the size.
     *
     * @return The height of the size.
     */
    public float getHeight() {
        return height;
    }

    /**
     * Get half of the height of the size.
     *
     * @return Half of the height of the size.
     */
    public float getHalfHeight() {
        return height / 2f;
    }

    /**
     * Sets the height of the size.
     *
     * @param height The height of the size.
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * Get the width of the size.
     *
     * @return The width of the size.
     */
    public float getWidth() {
        return width;
    }

    /**
     * Get half of the width of the size.
     *
     * @return Half of the width of the size.
     */
    public float getHalfWidth() {
        return width / 2f;
    }

    /**
     * Set the width of the size.
     *
     * @param width The width of the size.
     */
    public void setWidth(float width) {
        this.width = width;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Size) {
            return ((Size) obj).getWidth() == this.getWidth() && ((Size) obj).getHeight() == this.getHeight();
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s: [w: %f, h: %f]", this.getClass().getSimpleName(), getWidth(), getHeight());
    }
}
