/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.concept.commons;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.core.GameContext;

public class FpsCounter {

    private static final Logger log = LoggerFactory.getLogger(FpsCounter.class);
    private static final float logPeriodSec = 3f;

    private final GameContext context;
    private final String suffix;

    private float logPeriodElapsed = 0;

    private float elapsed = 0;
    private int count = 0;

    private int sampleSum;
    private int sampleCount;

    public FpsCounter(GameContext context) {
        this.context = context;
        this.suffix = "";
    }

    public FpsCounter(GameContext context, String suffix) {
        this.context = context;
        this.suffix = suffix;
    }

    public void update(DeltaTime delta) {
        count++;
        elapsed += delta.getElapsed();
        logPeriodElapsed += delta.getElapsed();

        if (elapsed + delta.getElapsed() > 1) {
            sampleSum += count;
            sampleCount++;

            if (logPeriodElapsed >= logPeriodSec) {
                final var output = "FPS average (in " + logPeriodSec + " seconds): " + (sampleSum / sampleCount);

                logPeriodElapsed = 0;
                sampleSum = 0;
                sampleCount = 0;

                // window calls must happen on the main (render) thread; this runs every few seconds only
                context.window().setTitle(output + "; " + suffix);
                log.trace(output);
            }

            elapsed = 0;
            count = 0;
        }
    }
}
