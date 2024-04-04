/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.common;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.graphics.Camera2D;
import org.pixel.graphics.GameWindowSettings;
import org.pixel.graphics.GameWindow;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

public abstract class DemoGame extends GameWindow {

    protected final static Logger log = LoggerFactory.getLogger(DemoGame.class);

    protected final Camera2D gameCamera;
    protected final FpsCounter fpsCounter;

    public DemoGame(GameWindowSettings settings) {
        super(settings);

        gameCamera = new Camera2D(this);
        fpsCounter = new FpsCounter();
    }

    @Override
    public void update(DeltaTime delta) {
        fpsCounter.update(delta);

        if (Keyboard.isKeyPressed(KeyboardKey.ESCAPE)) {
            close();
        }
    }
}
