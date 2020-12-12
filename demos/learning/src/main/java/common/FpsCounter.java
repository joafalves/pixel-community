/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package common;

import pixel.commons.logger.Logger;
import pixel.commons.logger.LoggerFactory;

public class FpsCounter {

    private static final Logger log = LoggerFactory.getLogger(FpsCounter.class);

    private float elapsed = 0;
    private float totalElapsed = 0;
    private int count = 0;
    private int last = 0;

    public boolean update(float delta) {
        count++;
        elapsed += delta;
        totalElapsed += delta;

        if (elapsed + delta > 1) {
            log.debug("FPS: %d", count);
            last = count;
            elapsed = 0;
            count = 0;

            return true;
        }

        return false;
    }

}
