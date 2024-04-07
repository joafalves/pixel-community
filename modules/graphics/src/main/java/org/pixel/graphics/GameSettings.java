/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics;

import lombok.Getter;
import lombok.Setter;

import org.pixel.commons.Color;

import java.util.Properties;

@Getter
@Setter
public class GameSettings {

    private static final String DEFAULT_TITLE = "Pixel Game";

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
    private boolean autoClear;

    //endregion

    //region Constructors

    /**
     * Constructor.
     *
     * @param virtualWidth The virtual width of the game viewport.
     * @param virtualHeight The virtual height of the game viewport.
     */
    public GameSettings(int virtualWidth, int virtualHeight) {
        this(DEFAULT_TITLE, virtualWidth, virtualHeight);
    }

    /**
     * Constructor.
     *
     * @param gameTitle The title of the game.
     * @param virtualWidth The virtual width of the game viewport.
     * @param virtualHeight The virtual height of the game viewport.
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
        this.autoClear = true;
        this.backgroundColor = Color.CORNFLOWER_BLUE;
    }

    //endregion
}
