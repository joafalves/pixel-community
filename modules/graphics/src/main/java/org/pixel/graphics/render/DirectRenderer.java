package org.pixel.graphics.render;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.math.Matrix4;

/**
 * Defines the contract for a renderer that draws objects individually, without batching.
 */
public interface DirectRenderer extends Initializable, Disposable {
    /**
     * Draws a single RenderCommand.
     *
     * @param renderable      The command to draw.
     * @param viewMatrix The camera's view-projection matrix.
     */
    void draw(Renderable renderable, Matrix4 viewMatrix);
}
