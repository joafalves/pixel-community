package org.pixel.graphics.render;

import org.pixel.math.Matrix4;

/**
 * Interface for renderers that support batching.
 * Batched renderers accumulate draw calls between begin() and end(),
 * then flush them all at once for optimal performance.
 */
public interface BatchRenderer extends Renderer {
    /**
     * Begin a batched rendering session.
     *
     * @param viewMatrix The camera's view-projection matrix
     */
    void begin(Matrix4 viewMatrix);

    /**
     * End the batched rendering session and flush all accumulated draw calls.
     */
    void end();
}
