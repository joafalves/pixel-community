/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

public abstract class DesktopWindowManager implements WindowManager, GameWindow {

    /**
     * Get the desktop window of a game, for desktop-only features (cursor, window mode, icon, ...).
     *
     * @param context The game context (running on the desktop host).
     * @return The desktop window manager.
     * @throws IllegalStateException If the game is not running on the desktop host.
     */
    public static DesktopWindowManager of(GameContext context) {
        if (context.window() instanceof DesktopWindowManager window) {
            return window;
        }
        throw new IllegalStateException("The game is not running on the desktop host.");
    }

    /**
     * Set the window dimensions.
     *
     * @param width  The width of the window.
     * @param height The height of the window.
     */
    public abstract void setWindowDimensions(int width, int height);

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
     * Switch between windowed and fullscreen.
     */
    public abstract void toggleFullscreen();

    @Override
    public int getWidth() {
        return getWindowWidth();
    }

    @Override
    public int getHeight() {
        return getWindowHeight();
    }

    @Override
    public float getPixelRatio() {
        return getWindowDimensions().getPixelRatio();
    }

    @Override
    public boolean isFocused() {
        return isWindowFocused();
    }

    @Override
    public void setTitle(String title) {
        setWindowTitle(title);
    }
}
