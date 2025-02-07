package org.pixel.core;

import org.pixel.math.Matrix4;

public interface Camera {
    /**
     * Get the camera projection matrix.
     *
     * @return The projection matrix.
     */
    Matrix4 getViewMatrix();
}
