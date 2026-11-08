package org.pixel.core;

/**
 * The surface a game draws to, in platform-neutral terms (a desktop window, an Android surface view, ...).
 * Platform hosts may expose more through their own window type.
 */
public interface GameWindow {

    /**
     * @return The drawable width in pixels (framebuffer size, i.e. including HiDPI scaling).
     */
    int getWidth();

    /**
     * @return The drawable height in pixels (framebuffer size, i.e. including HiDPI scaling).
     */
    int getHeight();

    /**
     * @return Framebuffer pixels per virtual (game) unit.
     */
    float getPixelRatio();

    /**
     * @return Whether the window currently has input focus.
     */
    boolean isFocused();

    /**
     * Set the window title (ignored where the platform has no title).
     *
     * @param title The title.
     */
    void setTitle(String title);
}
