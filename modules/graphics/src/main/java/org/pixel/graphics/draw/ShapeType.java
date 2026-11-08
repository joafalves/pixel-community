package org.pixel.graphics.draw;

/**
 * What the fragment shader evaluates for a {@code VertexLayout.FULL} quad. Codes are shared with the shaders.
 */
public enum ShapeType {

    /**
     * Textured quad (texture coordinates in uv).
     */
    TEXTURED(0),

    /**
     * Rectangle with per-corner radii: uv = half size, params = radii (top-left, top-right, bottom-right,
     * bottom-left).
     */
    RECT(1),

    /**
     * Ellipse (exact circle when both radii are equal): uv = radii.
     */
    ELLIPSE(2),

    /**
     * Line segment along the local x axis: uv = (half length, half width), params[0] = cap
     * (see {@code LineCap#code()}).
     */
    SEGMENT(3),

    /**
     * Path fill geometry (triangles and anti-aliasing fringes): local x = coverage (1 inside, 0 one pixel outside),
     * uv = position in path coordinates (for gradients).
     */
    PATH_FILL(4),

    /**
     * Path stroke segment along the local x axis: local y = signed distance from the center line, uv.x = half width.
     */
    PATH_STROKE(5),

    /**
     * Distance-field glyph (MTSDF atlas): uv = atlas coordinates, local = text coordinates (for gradients and the
     * pixel scale), params = (distance range and weight in text units, layer: 0 fill, 1 stroke, 2 shadow, unused).
     */
    GLYPH(6);

    private final int code;

    ShapeType(int code) {
        this.code = code;
    }

    /**
     * @return Whether commands of this type sample a texture.
     */
    public boolean isTextured() {
        return this == TEXTURED || this == GLYPH;
    }

    /**
     * @return The code written into vertices and tested by the shaders.
     */
    public int code() {
        return code;
    }
}
