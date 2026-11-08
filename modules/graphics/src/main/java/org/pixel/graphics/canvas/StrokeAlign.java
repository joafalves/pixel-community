package org.pixel.graphics.canvas;

/**
 * Where a stroke is drawn relative to the shape's edge.
 */
public enum StrokeAlign {
    /**
     * Entirely inside the shape (the shape keeps its size).
     */
    INSIDE,
    /**
     * Centered on the edge (half inside, half outside), like SVG/Canvas2D.
     */
    CENTER,
    /**
     * Entirely outside the shape.
     */
    OUTSIDE;

    /**
     * @param width The stroke width.
     * @return Signed offset of the stroke band's center from the edge (negative = inside).
     */
    public float offset(float width) {
        return switch (this) {
            case INSIDE -> -width * 0.5f;
            case CENTER -> 0f;
            case OUTSIDE -> width * 0.5f;
        };
    }

    /**
     * @param width The stroke width.
     * @return How far the stroke extends outside the shape's edge.
     */
    public float outsideExtent(float width) {
        return switch (this) {
            case INSIDE -> 0f;
            case CENTER -> width * 0.5f;
            case OUTSIDE -> width;
        };
    }
}
