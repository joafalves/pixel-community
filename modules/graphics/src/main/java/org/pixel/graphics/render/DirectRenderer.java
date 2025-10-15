package org.pixel.graphics.render;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.math.Matrix4;

/**
 * Defines the contract for a renderer that draws objects individually, without batching.
 * Used for sprites with custom shaders where batching is not possible.
 */
public interface DirectRenderer extends Renderer, Initializable, Disposable {
    /**
     * Draws a single renderable with a custom shader.
     *
     * @param renderable The renderable to draw.
     * @param viewMatrix The camera's view-projection matrix.
     */
    void draw(Renderable<?> renderable, Matrix4 viewMatrix);
}
