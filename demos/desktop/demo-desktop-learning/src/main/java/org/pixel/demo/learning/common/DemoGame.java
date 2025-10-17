/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.common;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.Timer;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.core.Camera2D;
import org.pixel.core.WindowSettings;
import org.pixel.core.Game;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.Vector2;

public abstract class DemoGame extends Game {

    protected final static Logger log = LoggerFactory.getLogger(DemoGame.class);

    protected final Camera2D gameCamera;

    private final Timer debugTimer = new Timer(1000);

    public DemoGame(WindowSettings settings) {
        super(settings);

        gameCamera = new Camera2D(settings.getViewportWidth(), settings.getViewportHeight());
        gameCamera.setOrigin(Vector2.half());
    }

    @Override
    public void update(DeltaTime delta) {
        if (debugTimer.elapsed()) {
            log.debug("Instant FPS: {0} - Smoothed FPS: {1}", getFps(), getSmoothedFps());
        }

        if (Keyboard.isKeyPressed(KeyboardKey.ESCAPE)) {
            dispose();
        }
    }
}
