/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

import org.pixel.math.Rectangle;

public abstract class StatefulRenderEngine extends RenderEngine {

    /**
     * Constructor
     *
     * @param viewportDimensions
     */
    public StatefulRenderEngine(Rectangle viewportDimensions) {
        super(viewportDimensions);
    }

    /**
     * Push current state into the render stack
     */
    public abstract void push();

    /**
     * Pop current state from the render stack, restoring the previous point
     */
    public abstract void pop();

}
