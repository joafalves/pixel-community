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
import org.pixel.core.GameApp;
import org.pixel.core.GameSettings;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.Vector2;

public abstract class DemoGame extends GameApp {

    protected final static Logger log = LoggerFactory.getLogger(DemoGame.class);

    protected Camera2D gameCamera;

    /**
     * Demo settings: dev mode on and the engine's dev tools open from the start (F1 hides them).
     */
    protected static <S extends GameSettings> S withDevTools(S settings) {
        settings.setDevMode(true);
        settings.setDevToolsVisible(true);
        return settings;
    }

    private final Timer debugTimer = new Timer(1000);

    /**
     * Creates the demo camera. Subclasses overriding {@code load()} call {@code super.load()} first.
     */
    @Override
    public void load() {
        gameCamera = new Camera2D(context());
        gameCamera.setOrigin(Vector2.half());
    }

    @Override
    public void update(DeltaTime delta) {
        if (settings().isDevMode() && debugTimer.elapsed()) {
            log.debug("Instant FPS: {0} - Smoothed FPS: {1}", getFps(), getSmoothedFps());
        }

        if (Keyboard.isKeyPressed(KeyboardKey.ESCAPE)) {
            exit();
        }
    }
}
