/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

import lombok.Getter;
import lombok.Setter;

import org.pixel.commons.Color;
import org.pixel.graphics.GraphicsBackend;

import java.util.Properties;

@Getter
@Setter
public class GameSettings {

    //region Fields & Properties

    private GraphicsBackend graphicsBackend;
    private String title;
    private Properties clientProperties;
    private Color backgroundColor;
    private int virtualWidth, virtualHeight;
    private int multisampling;
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
    public GameSettings(int virtualWidth, int virtualHeight) {
        this("Pixel Game", virtualWidth, virtualHeight);
    }

    /**
     * Constructor.
     *
     * @param gameTitle The title of the game.
     * @param virtualWidth The virtual width of the window.
     * @param virtualHeight The virtual height of the window.
     */
    public GameSettings(String gameTitle, int virtualWidth, int virtualHeight) {
        this.graphicsBackend = GraphicsBackend.OpenGL;
        this.title = gameTitle;
        this.virtualWidth = virtualWidth;
        this.virtualHeight = virtualHeight;
        this.idleThrottle = true;
        this.vsync = true;
        this.multisampling = 0;
        this.debugMode = false;
        this.autoWindowClear = true;
        this.backgroundColor = Color.CORNFLOWER_BLUE;
    }

    //endregion
}
