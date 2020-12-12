/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.core;

import pixel.graphics.Color;

import java.util.Properties;

public class GameSettings {

    //region Fields & Properties

    private int windowWidth, windowHeight;
    private int virtualWidth, virtualHeight;
    private int multisampling;
    private boolean windowResizable;
    private boolean vsync;
    private boolean debugMode;
    private String windowTitle;
    private WindowMode windowMode;
    private Properties clientProperties;
    private Color backgroundColor;

    //endregion

    //region Constructors

    /**
     * Constructor
     *
     * @param virtualWidth
     * @param virtualHeight
     */
    public GameSettings(int virtualWidth, int virtualHeight) {
        this.windowTitle = "Pixel Game Window";
        this.virtualWidth = virtualWidth;
        this.virtualHeight = virtualHeight;
        this.windowWidth = virtualWidth;
        this.windowHeight = virtualHeight;
        this.windowResizable = false;
        this.vsync = true;
        this.windowMode = WindowMode.WINDOWED;
        this.multisampling = 0;
        this.debugMode = false;
        this.backgroundColor = Color.CORNFLOWER_BLUE;
    }

    //endregion

    //region Public Functions

    public int getVirtualWidth() {
        return virtualWidth;
    }

    public void setVirtualWidth(int virtualWidth) {
        this.virtualWidth = virtualWidth;
    }

    public int getVirtualHeight() {
        return virtualHeight;
    }

    public void setVirtualHeight(int virtualHeight) {
        this.virtualHeight = virtualHeight;
    }

    public boolean isWindowResizable() {
        return windowResizable;
    }

    public void setWindowResizable(boolean windowResizable) {
        this.windowResizable = windowResizable;
    }

    public String getWindowTitle() {
        return windowTitle;
    }

    public void setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
    }

    public boolean isVsyncEnabled() {
        return vsync;
    }

    public void setVsync(boolean enabled) {
        this.vsync = enabled;
    }

    public WindowMode getWindowMode() {
        return windowMode;
    }

    public void setWindowMode(WindowMode mode) {
        this.windowMode = mode;
    }

    public int getMultisampling() {
        return multisampling;
    }

    public void setMultisampling(int multisampling) {
        this.multisampling = multisampling;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public Properties getClientProperties() {
        return clientProperties;
    }

    public void setClientProperties(Properties clientProperties) {
        this.clientProperties = clientProperties;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    //endregion
}
