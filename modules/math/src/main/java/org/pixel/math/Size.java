/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.io.Serializable;

public class Size implements Serializable {

    private int width;
    private int height;

    /**
     * Constructor
     *
     * @param width
     * @param height
     */
    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Constructor
     *
     * @param other
     */
    public Size(Size other) {
        this.width = other.width;
        this.height = other.height;
    }

    public void set(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHalfWidth() {
        return width / 2;
    }

    public int getHalfHeight() {
        return height / 2;
    }

    public void setWidth(int width) {
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
        return String.format("%s: [w: %d, h: %d]", this.getClass().getSimpleName(), getWidth(), getHeight());
    }
}
