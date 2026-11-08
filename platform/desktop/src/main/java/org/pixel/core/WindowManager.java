package org.pixel.core;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;

public interface WindowManager extends Initializable, Disposable {

    /**
     * Window manager begin frame method.
     */
    void beginFrame();

    /**
     * Window manager end frame method.
     */
    void endFrame();

    /**
     * Get the window handle.
     * 
     * @return The window handle.
     */
    long getWindowHandle();

    /**
     * Determine if the window is active.
     * 
     * @return True if the window is active, false otherwise.
     */
    boolean isWindowActive();

    /**
     * Request the window to close. The game loop finishes the current frame and then disposes the game.
     * Safe to call from any game callback (update, draw, event listeners).
     */
    void requestClose();

    /**
     * Determine if the window is focused.
     * 
     * @return True if the window is focused, false otherwise.
     */
    boolean isWindowFocused();

    /**
     * Set V-SYNC mode.
     * 
     * @param enabled True if V-SYNC should be enabled, false otherwise.
     */
    void setVSync(boolean enabled);

    /**
     * @return Framebuffer pixels per window (screen) coordinate: the factor from mouse positions to pixels (2 on a
     * Retina display, 1 on Windows and Linux). Kept up to date when the window is resized or changes monitor.
     */
    float getFramebufferScale();

    /**
     * @return The OS content scale of the window (2 on Retina, 1.5 at 150% on Windows). Kept up to date.
     */
    float getContentScale();
}
