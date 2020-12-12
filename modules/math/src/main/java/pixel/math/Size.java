/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.math;

public class Size {

    private Integer width;
    private Integer height;

    /**
     * Constructor
     */
    public Size() {
    }

    /**
     * Constructor
     *
     * @param width
     * @param height
     */
    public Size(Integer width, Integer height) {
        this.width = width;
        this.height = height;
    }

    public void set(Integer width, Integer height) {
        this.width = width;
        this.height = height;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }
}
