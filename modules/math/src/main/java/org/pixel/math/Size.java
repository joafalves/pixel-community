/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

public class Size {

    private int width;
    private int height;

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
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
