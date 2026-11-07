package org.pixel.graphics.canvas;

/**
 * How the ends of a line are drawn.
 */
public enum LineCap {
    /**
     * Flat end exactly at the end point.
     */
    BUTT(0),
    /**
     * Half-circle around the end point.
     */
    ROUND(1),
    /**
     * Flat end extended by half the line width.
     */
    SQUARE(2);

    private final int code;

    LineCap(int code) {
        this.code = code;
    }

    /**
     * @return The code passed to the shader.
     */
    public int code() {
        return code;
    }
}
