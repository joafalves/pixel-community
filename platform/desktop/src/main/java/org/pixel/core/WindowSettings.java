/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WindowSettings extends GameSettings {

    private static final String DEFAULT_TITLE = "Pixel Desktop Game";

    private int windowWidth, windowHeight;
    private boolean windowResizable;
    private WindowMode windowMode;

    /**
     * Constructor.
     *
     * @param virtualWidth  The virtual width of the window.
     * @param virtualHeight The virtual height of the window.
     */
    public WindowSettings(int virtualWidth, int virtualHeight) {
        this(DEFAULT_TITLE, virtualWidth, virtualHeight);
    }

    /**
     * Constructor.
     *
     * @param gameTitle     The title of the game (applies on game window).
     * @param virtualWidth  The virtual width of the window.
     * @param virtualHeight The virtual height of the window.
     */
    public WindowSettings(String gameTitle, int virtualWidth, int virtualHeight) {
        super(gameTitle, virtualWidth, virtualHeight);
        this.windowWidth = virtualWidth;
        this.windowHeight = virtualHeight;
        this.windowResizable = false;
        this.windowMode = WindowMode.WINDOWED;
    }
}
