/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

public interface WindowEventListener {

    /**
     * Triggers when the window size changes.
     *
     * @param newWidth - new width of the window
     * @param newHeight - new height of the window
     */
    void windowSizeChanged(int newWidth, int newHeight);

    /**
     * Triggers when the window mode changes.
     *
     * @param windowMode - new window mode
     */
    void windowModeChanged(WindowMode windowMode);
}
