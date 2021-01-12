/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.learning.common;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

public class FpsCounter {

    private static final Logger log = LoggerFactory.getLogger(FpsCounter.class);

    private float elapsed = 0;
    private int count = 0;

    public void update(float delta) {
        count++;
        elapsed += delta;

        if (elapsed + delta > 1) {
            log.debug("FPS: %d", count);

            elapsed = 0;
            count = 0;
        }
    }

}
