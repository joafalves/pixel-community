/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.graphics.render;

import pixel.commons.lifecycle.Disposable;

public interface PostProcessor extends Disposable {

    /**
     * Start the post processing phase
     */
    void begin();

    /**
     * End the post processing phase
     */
    void end();

    /**
     * Apply post processing
     *
     * @param delta
     */
    void apply(float delta);

}
