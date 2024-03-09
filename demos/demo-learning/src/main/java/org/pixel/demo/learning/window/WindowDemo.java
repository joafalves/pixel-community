/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.window;

import org.pixel.commons.DeltaTime;
import org.pixel.core.GameWindow;
import org.pixel.core.WindowSettings;

public class WindowDemo extends GameWindow {

    public WindowDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {

    }

    @Override
    public void update(DeltaTime delta) {

    }

    @Override
    public void draw(DeltaTime delta) {

    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(800, 600);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(false);

        GameWindow window = new WindowDemo(settings);
        window.start();
    }
}
