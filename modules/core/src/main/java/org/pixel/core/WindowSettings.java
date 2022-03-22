/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

import lombok.Getter;
import lombok.Setter;
import org.pixel.graphics.Color;

import java.util.Properties;

@Getter
@Setter
public class WindowSettings {

    //region Fields & Properties

    private String windowTitle;
    private WindowMode windowMode;
    private Properties clientProperties;
    private Color backgroundColor;
    private int windowWidth, windowHeight;
    private int virtualWidth, virtualHeight;
    private int multisampling;
    private boolean windowResizable;
    private boolean vsync;
    private boolean debugMode;
    private boolean idleThrottle;
    private boolean autoWindowClear;

    //endregion

    //region Constructors

    /**
     * Constructor.
     *
     * @param virtualWidth The virtual width of the window.
     * @param virtualHeight The virtual height of the window.
     */
    public WindowSettings(int virtualWidth, int virtualHeight) {
        this.windowTitle = "Pixel Window";
        this.virtualWidth = virtualWidth;
        this.virtualHeight = virtualHeight;
        this.windowWidth = virtualWidth;
        this.windowHeight = virtualHeight;
        this.windowResizable = false;
        this.idleThrottle = true;
        this.vsync = true;
        this.windowMode = WindowMode.WINDOWED;
        this.multisampling = 0;
        this.debugMode = false;
        this.autoWindowClear = true;
        this.backgroundColor = Color.CORNFLOWER_BLUE;
    }

    //endregion
}
