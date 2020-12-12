/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package common;

import pixel.commons.logger.Logger;
import pixel.commons.logger.LoggerFactory;
import pixel.core.Game;
import pixel.core.GameSettings;

public abstract class DemoGame extends Game {

    protected final static Logger log = LoggerFactory.getLogger(FpsCounter.class);

    private FpsCounter fpsCounter;

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
