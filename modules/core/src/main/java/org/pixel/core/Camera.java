package org.pixel.core;

import org.pixel.graphics.pass.View;
import org.pixel.math.Matrix4;

/**
 * A camera: a {@link View} games move around (render pipeline passes draw through it).
 */
public interface Camera extends View {
    /**
     * Get the camera projection matrix.
     *
     * @return The projection matrix.
     */
    Matrix4 getViewMatrix();
}
