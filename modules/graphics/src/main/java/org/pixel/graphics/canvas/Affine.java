package org.pixel.graphics.canvas;

/**
 * Mutable 2D affine transform: {@code x' = a*x + c*y + tx, y' = b*x + d*y + ty}. Operations post-multiply (the new
 * operation applies to coordinates first), like Canvas2D/SVG transforms.
 */
final class Affine {

    float a, b, c, d, tx, ty;

    Affine() {
        identity();
    }

    void identity() {
        a = 1;
        b = 0;
        c = 0;
        d = 1;
        tx = 0;
        ty = 0;
    }


    void translate(float x, float y) {
        tx += a * x + c * y;
        ty += b * x + d * y;
    }

    /**
     * Rotate by {@code radians}; positive values rotate clockwise on screen (y axis pointing down), matching sprite
     * rotation.
     */
    void rotate(float radians) {
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float na = a * cos + c * sin;
        float nb = b * cos + d * sin;
        float nc = c * cos - a * sin;
        float nd = d * cos - b * sin;
        a = na;
        b = nb;
        c = nc;
        d = nd;
    }

    void scale(float sx, float sy) {
        a *= sx;
        b *= sx;
        c *= sy;
        d *= sy;
    }

    float mapX(float x, float y) {
        return a * x + c * y + tx;
    }

    float mapY(float x, float y) {
        return b * x + d * y + ty;
    }

    /**
     * @return Whether axis-aligned rectangles stay axis-aligned (no rotation or skew).
     */
    boolean isAxisAligned() {
        return Math.abs(b) < 1e-6f && Math.abs(c) < 1e-6f;
    }

    /**
     * @return The smaller of the two axis scale factors (used to size anti-aliasing margins in local units).
     */
    float minScale() {
        float sx = (float) Math.sqrt(a * a + b * b);
        float sy = (float) Math.sqrt(c * c + d * d);
        return Math.min(sx, sy);
    }
}
