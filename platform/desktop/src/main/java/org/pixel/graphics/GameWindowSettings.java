/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameWindowSettings extends BaseGameWindowSettings {

    private int windowWidth, windowHeight;
    private boolean windowResizable;
    private WindowMode windowMode;

    /**
     * Constructor.
     *
     * @param virtualWidth  The virtual width of the window.
     * @param virtualHeight The virtual height of the window.
     */
    public GameWindowSettings(int virtualWidth, int virtualHeight) {
        super(virtualWidth, virtualHeight);
        this.windowWidth = virtualWidth;
        this.windowHeight = virtualHeight;
        this.windowResizable = false;
        this.windowMode = WindowMode.WINDOWED;
    }

    /**
     * Constructor.
     *
     * @param gameTitle     The title of the game.
     * @param virtualWidth  The virtual width of the window.
     * @param virtualHeight The virtual height of the window.
     */
    public GameWindowSettings(String gameTitle, int virtualWidth, int virtualHeight) {
        super(gameTitle, virtualWidth, virtualHeight);
    }
}
