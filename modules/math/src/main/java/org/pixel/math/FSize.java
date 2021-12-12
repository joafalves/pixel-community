/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.io.Serializable;

public class FSize implements Serializable {

    private float width;
    private float height;

    /**
     * Constructor
     */
    public FSize() {
        this.width = 0.f;
        this.height = 0.f;
    }

    /**
     * Constructor
     *
     * @param width
     * @param height
     */
    public FSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Constructor
     *
     * @param other
     */
    public FSize(float other) {
        this.width = other;
        this.height = other;
    }

    public void addWidth(float value) {
        this.width = width + value;
    }

    public void subtractWidth(float value) {
        this.width = width - value;
    }

    public void addHeight(float value) {
        this.height = height + value;
    }

    public void subtractHeight(float value) {
        this.height = height - value;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FSize) {
            return ((FSize) obj).getWidth() == this.getWidth() && ((FSize) obj).getHeight() == this.getHeight();
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s: [w: %f, h: %f]", this.getClass().getSimpleName(), getWidth(), getHeight());
    }
}
