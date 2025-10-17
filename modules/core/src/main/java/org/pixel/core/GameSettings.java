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

    private static final String DEFAULT_TITLE = "Pixel Game";

    //region Fields & Properties

    private GraphicsBackend graphicsBackend;
    private String title;
    private Properties clientProperties;
    private Color backgroundColor;
    private int viewportWidth;
    private int viewportHeight;
    private int multisampling;
    private int targetFps;
    private boolean vsync;
    private boolean devMode;
    private boolean idleThrottle;
    private boolean autoClear;
    private String[] blueprintPackages;

    //endregion

    //region Constructors

    /**
     * Constructor.
     *
     * @param viewportWidth The game viewport width.
     * @param viewportHeight The game viewport height.
     */
    public GameSettings(int viewportWidth, int viewportHeight) {
        this(DEFAULT_TITLE, viewportWidth, viewportHeight);
    }

    /**
     * Constructor.
     *
     * @param gameTitle The title of the game.
     * @param viewportWidth The virtual width of the game viewport.
     * @param viewportHeight The virtual height of the game viewport.
     */
    public GameSettings(String gameTitle, int viewportWidth, int viewportHeight) {
        this.graphicsBackend = GraphicsBackend.OpenGL;
        this.title = gameTitle;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.idleThrottle = true;
        this.vsync = false;
        this.multisampling = 0;
        this.devMode = false;
        this.autoClear = true;
        this.targetFps = 0;
        this.backgroundColor = Color.CORNFLOWER_BLUE;
    }

    //endregion
}
