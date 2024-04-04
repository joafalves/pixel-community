/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.math.Matrix4;

public interface DrawBatch extends Disposable {

    //region public methods

    /**
     * Begin drawing phase.
     *
     * @param viewMatrix The view matrix.
     */
    void begin(Matrix4 viewMatrix);

    /**
     * End drawing phase.
     */
    void end();

    //endregion
}
