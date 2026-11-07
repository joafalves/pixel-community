package org.pixel.graphics;

import org.pixel.commons.Color;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.render.SpriteBatch;

public interface GraphicsDevice extends Initializable, Disposable {

    /**
     * Clear the screen.
     */
    void clear();

    /**
     * Set the clear color.
     * 
     * @param color The clear color.
     */
    void setClearColor(Color color);

    /**
     * Read pixels from the screen (RGBA chunk format).
     * 
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param width The width.
     * @param height The height.
     * @param opaque True if the alpha channel should be opaque, false otherwise.
     * @return The pixels (RGBA chunk format).
     */
    byte[] readPixels(int x, int y, int width, int height, boolean opaque);

    /**
     * Get the rendering counters of this device. Renderers record into the frame in progress; read the last completed
     * frame with {@code getStats().lastFrame()}.
     *
     * @return The render statistics of this device.
     */
    RenderStats getStats();

    /**
     * Mark the end of a frame (called by the game loop after the frame has been drawn).
     */
    void endFrame();

    /**
     * Get the URP v2 render backend (all GPU work of draw-list based renderers goes through it).
     *
     * @return The render backend.
     * @throws UnsupportedOperationException On platforms without a URP backend yet.
     */
    RenderBackend getRenderBackend();

    /**
     * Create a sprite batch for this device. The caller owns it and must dispose it.
     *
     * @return A new, initialized sprite batch.
     */
    SpriteBatch createSpriteBatch();
}
