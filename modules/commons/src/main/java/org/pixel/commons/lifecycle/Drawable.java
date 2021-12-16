/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.lifecycle;

import org.pixel.commons.DeltaTime;

public interface Drawable {

    /**
     * Draw function.
     *
     * @param delta Time since last draw.
     */
    void draw(DeltaTime delta);
}
