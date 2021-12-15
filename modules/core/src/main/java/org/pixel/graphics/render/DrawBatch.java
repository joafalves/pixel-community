/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.math.Matrix4;

public abstract class DrawBatch implements Disposable {

    //region public methods

    /**
     * Begin drawing phase.
     *
     * @param viewMatrix The view matrix.
     */
    public abstract void begin(Matrix4 viewMatrix);

    /**
     * End drawing phase.
     */
    public abstract void end();

    //endregion
}
