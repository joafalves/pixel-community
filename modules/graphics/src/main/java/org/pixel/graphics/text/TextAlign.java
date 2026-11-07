package org.pixel.graphics.text;

/**
 * Horizontal alignment of lines inside a text block.
 */
public enum TextAlign {
    LEFT,
    CENTER,
    RIGHT;

    /**
     * @return The fraction of the free space placed before a line (0, 0.5 or 1).
     */
    public float factor() {
        return switch (this) {
            case LEFT -> 0f;
            case CENTER -> 0.5f;
            case RIGHT -> 1f;
        };
    }
}
