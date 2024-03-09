/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.concept.commons;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.core.GameWindow;

public class FpsCounter {

    private static final Logger log = LoggerFactory.getLogger(FpsCounter.class);

    private final GameWindow window;
    private final String suffix;

    private final float logPeriodSec = 5;
    private float logPeriodElapsed = 0;

    private float elapsed = 0;
    private int count = 0;

    private int sampleSum;
    private int sampleCount;

    public FpsCounter(GameWindow window) {
        this.window = window;
        this.suffix = "";
    }

    public FpsCounter(GameWindow window, String suffix) {
        this.window = window;
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

                // setWindowTitle at every second takes its toll if executed on the main thread, avoid!
                new Thread(() -> window.setWindowTitle(output + "; " + suffix)).start();
                log.trace(output);
            }

            elapsed = 0;
            count = 0;
        }
    }
}
