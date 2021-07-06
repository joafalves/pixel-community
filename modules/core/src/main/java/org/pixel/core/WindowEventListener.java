/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

public interface WindowEventListener {

    /**
     * Triggers when the window size changes
     *
     * @param newWidth
     * @param newHeight
     */
    void windowSizeChanged(int newWidth, int newHeight);

    /**
     * Triggers when the window mode changes
     *
     * @param windowMode
     */
    void windowModeChanged(WindowMode windowMode);
}
