/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics;

public interface DesktopWindowManager extends WindowManager {

    /**
     * Set the window dimensions.
     * 
     * @param width  The width of the window.
     * @param height The height of the window.
     */
    void setWindowDimensions(int width, int height);

    /**
     * Set the window cursor mode.
     * 
     * @param mode The cursor mode.
     */
    void setWindowCursorMode(WindowCursorMode mode);

    /**
     * Set the window mode.
     * 
     * @param mode The window mode.
     */
    void setWindowMode(WindowMode mode);

    /**
     * Set the window title.
     * 
     * @param title The title of the window.
     */
    void setWindowTitle(String title);

    /**
     * Set the window icon.
     * 
     * @param iconPaths The icon filenames ordered from highest to lowest resolution
     *                  (at least one valid filepath must be provided). Supports
     *                  relative and absolute paths.
     */
    void setWindowIcon(String... iconPaths);

    /**
     * Get window dimensions.
     * 
     * @return The window dimensions.
     */
    WindowDimensions getWindowDimensions();
}
