package org.pixel.graphics.pass;

import org.pixel.math.Matrix4;

/**
 * What a render pass looks at: maps layer (world) coordinates to clip space. {@code Camera2D} is a view; so is
 * {@code ScreenSpace} (HUD). The pipeline derives culling bounds from the inverse of the matrix, so any affine view
 * works.
 */
@FunctionalInterface
public interface View {

    /**
     * @return The projection·view matrix (layer coordinates to clip space). Callers don't keep or modify it.
     */
    Matrix4 getViewMatrix();

    /**
     * Framebuffer pixels per layer unit, e.g. {@code pixelRatio × zoom} for a camera. Canvases use it to keep
     * anti-aliased edges one pixel wide under any zoom or display density.
     *
     * @return Pixels per unit (1 by default).
     */
    default float getPixelsPerUnit() {
        return 1f;
    }
}
