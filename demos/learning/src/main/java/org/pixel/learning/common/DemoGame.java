/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.learning.common;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.core.Game;
import org.pixel.core.GameSettings;

public abstract class DemoGame extends Game {

    protected final static Logger log = LoggerFactory.getLogger(DemoGame.class);

    private final FpsCounter fpsCounter;

    /**
     * Constructor
     *
     * @param settings
     */
    public DemoGame(GameSettings settings) {
        super(settings);

        fpsCounter = new FpsCounter();
    }

    @Override
    public void update(float delta) {
        fpsCounter.update(delta);
    }
}
