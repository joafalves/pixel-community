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
}
