/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.learning.common;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.commons.DeltaTime;
import org.pixel.core.Camera2D;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;

public abstract class DemoGame extends PixelWindow {

    protected final static Logger log = LoggerFactory.getLogger(DemoGame.class);

    protected final Camera2D gameCamera;
    protected final FpsCounter fpsCounter;

    /**
     * Constructor
     *
     * @param settings
     */
    public DemoGame(WindowSettings settings) {
        super(settings);

        gameCamera = new Camera2D(this);
        fpsCounter = new FpsCounter();
    }

    @Override
    public void update(DeltaTime delta) {
        fpsCounter.update(delta);
    }
}
