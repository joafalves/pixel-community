package org.pixel.graphics;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.core.CursorMode;
import org.pixel.core.WindowDimensions;
import org.pixel.core.WindowMode;

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
     * Set the window dimensions.
     * 
     * @param width  The width of the window.
     * @param height The height of the window.
     */
    void setWindowDimensions(int width, int height);

    /**
     * Set the window dimensions.
     * 
     * @param dimensions The window dimensions.
     */
    void setCursorMode(CursorMode mode);

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
     * Set V-SYNC mode.
     * 
     * @param enabled True if V-SYNC should be enabled, false otherwise.
     */
    void setVSync(boolean enabled);

    /**
     * Get window dimensions.
     * 
     * @return The window dimensions.
     */
    WindowDimensions getWindowDimensions();

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
}
