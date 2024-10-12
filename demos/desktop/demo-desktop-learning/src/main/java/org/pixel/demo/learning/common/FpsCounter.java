/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.common;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

public class FpsCounter {

    private static final Logger log = LoggerFactory.getLogger(FpsCounter.class);

    private float elapsed = 0;
    private int count = 0;

    public void update(DeltaTime delta) {
        count++;
        elapsed += delta.getElapsed();

        if (elapsed + delta.getElapsed() > 1) {
            log.debug("FPS: {}.", count);

            elapsed = 0;
            count = 0;
        }
    }

}
