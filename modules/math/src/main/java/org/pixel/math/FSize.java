/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

public class FSize {

    private Float width;
    private Float height;

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
    public FSize(Float width, Float height) {
        this.width = width;
        this.height = height;
    }

    public void addWidth(Float value) {
        this.width = width == null ? value : width + value;
    }

    public void subtractWidth(Float value) {
        this.width = width == null ? -value : width - value;
    }

    public void addHeight(Float value) {
        this.height = height == null ? value : height + value;
    }

    public void subtractHeight(Float value) {
        this.height = height == null ? -value : height - value;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public Float getWidth() {
        return width;
    }

    public void setWidth(Float width) {
        this.width = width;
    }
}
