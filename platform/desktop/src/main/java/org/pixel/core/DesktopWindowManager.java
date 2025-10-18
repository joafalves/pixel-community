/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

public abstract class DesktopWindowManager implements WindowManager {

    /**
     * Set the window dimensions.
     *
     * @param width  The width of the window.
     * @param height The height of the window.
     */
    public abstract void setWindowSize(int width, int height);

    /**
     * Set the window cursor mode.
     *
     * @param mode The cursor mode.
     */
    public abstract void setWindowCursorMode(WindowCursorMode mode);

    /**
     * Set the window cursor type.
     *
     * @param type The cursor type.
     */
    public abstract void setWindowCursorType(WindowCursorType type);

    /**
     * Set the window mode.
     *
     * @param mode The window mode.
     */
    public abstract void setWindowMode(WindowMode mode);

    /**
     * Set the window title.
     *
     * @param title The title of the window.
     */
    public abstract void setWindowTitle(String title);

    /**
     * Set the window icon.
     *
     * @param iconPaths The icon filenames ordered from highest to lowest resolution
     *                  (at least one valid filepath must be provided). Supports
     *                  relative and absolute paths.
     */
    public abstract void setWindowIcon(String... iconPaths);

    /**
     * Get window dimensions.
     *
     * @return The window dimensions.
     */
    public abstract WindowDimensions getWindowDimensions();

    /**
     * Get window width.
     *
     * @return The window width.
     */
    public int getWindowWidth() {
        return getWindowDimensions().getWindowWidth();
    }

    /**
     * Get window height.
     *
     * @return The window height.
     */
    public int getWindowHeight() {
        return getWindowDimensions().getWindowHeight();
    }

    /**
     * Request that the window be closed. Default implementations should
     * forward to platform-specific behavior.
     */
    @Override
    public void requestClose() {
        // Default no-op, platform implementations may override for proper behavior
    }
}
