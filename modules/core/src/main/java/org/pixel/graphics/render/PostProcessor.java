/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.DeltaTime;

public interface PostProcessor extends Disposable {

    /**
     * Start the post-processing phase
     */
    void begin();

    /**
     * End the post-processing phase
     */
    void end();

    /**
     * Apply post-processing (this function will execute end() automatically)
     *
     * @param delta
     */
    void apply(DeltaTime delta);

}
