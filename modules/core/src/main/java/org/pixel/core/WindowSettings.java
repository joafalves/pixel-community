/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

import lombok.Getter;
import lombok.Setter;
import org.pixel.graphics.Color;
import org.pixel.graphics.GraphicsBackend;

import java.util.Properties;

@Getter
@Setter
public class WindowSettings {

    //region Fields & Properties

    private GraphicsBackend graphicsBackend;
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
        this("Pixel Window", virtualWidth, virtualHeight);
    }

    /**
     * Constructor.
     *
     * @param windowTitle The title of the window.
     * @param virtualWidth The virtual width of the window.
     * @param virtualHeight The virtual height of the window.
     */
    public WindowSettings(String windowTitle, int virtualWidth, int virtualHeight) {
        this.graphicsBackend = GraphicsBackend.OpenGL;
        this.windowTitle = windowTitle;
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
