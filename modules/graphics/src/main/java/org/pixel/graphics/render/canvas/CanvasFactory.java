package org.pixel.graphics.render.canvas;

import org.pixel.commons.factory.Factory;

/**
 * Factory interface for creating platform-specific Canvas instances.
 *
 * <p>Canvas instances require viewport dimensions at creation time and are not singletons.
 * Each call to {@code create()} produces a new independent Canvas instance.
 */
public interface CanvasFactory extends Factory {

    /**
     * Create a Canvas with the specified viewport dimensions.
     *
     * @param width  Viewport width in pixels
     * @param height Viewport height in pixels
     * @return A new Canvas instance appropriate for the current platform
     */
    Canvas create(int width, int height);
}
