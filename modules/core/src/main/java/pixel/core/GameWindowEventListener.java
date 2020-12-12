/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.core;

public interface GameWindowEventListener {

    /**
     * Triggers when the window size changes
     *
     * @param newWidth
     * @param newHeight
     */
    void gameWindowSizeChanged(int newWidth, int newHeight);

    /**
     * Triggers when the game window mode changes
     *
     * @param windowMode
     */
    void gameWindowModeChanged(WindowMode windowMode);
}
