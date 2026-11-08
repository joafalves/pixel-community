package org.pixel.graphics.backend;

import org.pixel.graphics.render.BlendMode;
import org.pixel.math.Matrix4;

/**
 * GPU state for one pass: the view-projection matrix and blend mode. Reusable (set, submit, set again).
 */
public final class PassState {

    private final Matrix4 viewMatrix = new Matrix4();
    private BlendMode blendMode = BlendMode.NORMAL_BLEND;
    private RenderTarget target;
    private boolean clearTarget;

    /**
     * Set the state for drawing to the screen. The matrix is copied, so the caller may change its own matrix
     * afterwards.
     *
     * @param viewMatrix The view-projection matrix.
     * @param blendMode  The blend mode.
     * @return This instance.
     */
    public PassState set(Matrix4 viewMatrix, BlendMode blendMode) {
        if (viewMatrix == null || blendMode == null) {
            throw new IllegalArgumentException("View matrix and blend mode cannot be null.");
        }
        float[][] src = viewMatrix.toUnsafeArray();
        float[][] dst = this.viewMatrix.toUnsafeArray();
        for (int i = 0; i < 4; i++) {
            System.arraycopy(src[i], 0, dst[i], 0, 4);
        }
        this.blendMode = blendMode;
        this.target = null;
        this.clearTarget = false;
        return this;
    }

    /**
     * Draw into a target instead of the screen (call after {@link #set}).
     *
     * @param target The target.
     * @param clear  Whether to clear it to its clear color first.
     */
    public PassState target(RenderTarget target, boolean clear) {
        this.target = target;
        this.clearTarget = target != null && clear;
        return this;
    }

    /** The target to draw into, or {@code null} for the screen. */
    public RenderTarget getTarget() {
        return target;
    }

    public boolean isClearTarget() {
        return clearTarget;
    }

    public Matrix4 getViewMatrix() {
        return viewMatrix;
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }
}
