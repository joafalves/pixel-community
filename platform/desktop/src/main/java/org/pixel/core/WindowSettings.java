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

    private int windowWidth;
    private int windowHeight;
    private boolean windowResizable;
    private boolean windowDecorated;
    private boolean glfwDebugMode;
    private boolean windowHighDpi;
    private boolean highPriorityProcess;
    private WindowMode windowMode;

    /**
     * Constructor.
     *
     * @param viewportWidth  The virtual width of the window.
     * @param viewportHeight The virtual height of the window.
     */
    public WindowSettings(int viewportWidth, int viewportHeight) {
        this(DEFAULT_TITLE, viewportWidth, viewportHeight);
    }

    /**
     * Constructor.
     *
     * @param gameTitle     The title of the game (applies on game window).
     * @param viewportWidth  The virtual width of the window.
     * @param viewportHeight The virtual height of the window.
     */
    public WindowSettings(String gameTitle, int viewportWidth, int viewportHeight) {
        super(gameTitle, viewportWidth, viewportHeight);
        this.windowWidth = viewportWidth;
        this.windowHeight = viewportHeight;
        this.windowResizable = false;
        this.windowMode = WindowMode.WINDOWED;
        this.windowDecorated = true;
        this.windowHighDpi = true;
        this.glfwDebugMode = false;
        this.highPriorityProcess = true;
    }
}
