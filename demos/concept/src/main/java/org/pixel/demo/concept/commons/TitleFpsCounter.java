/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.concept.commons;

import org.pixel.commons.DeltaTime;
import org.pixel.core.PixelWindow;

public class TitleFpsCounter {

    private final PixelWindow window;

    private float elapsed = 0;
    private int count = 0;

    public TitleFpsCounter(PixelWindow window) {
        this.window = window;
    }

    public void update(DeltaTime delta) {
        count++;
        elapsed += delta.getElapsed();

        if (elapsed + delta.getElapsed() > 1) {
            window.setWindowTitle("FPS: " + count);

            elapsed = 0;
            count = 0;
        }
    }
}
