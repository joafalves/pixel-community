/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.graphics.render;

import pixel.commons.lifecycle.Disposable;
import pixel.math.Matrix4;

public abstract class DrawBatch implements Disposable {

    //region public methods

    /**
     * Begin drawing phase
     *
     * @param viewMatrix
     */
    public abstract void begin(Matrix4 viewMatrix);

    /**
     * End drawing phase
     */
    public abstract void end();

    //endregion
}
