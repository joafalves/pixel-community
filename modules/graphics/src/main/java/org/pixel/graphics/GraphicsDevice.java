package org.pixel.graphics;

import org.pixel.commons.Color;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;

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
}
