package org.pixel.graphics.render;

import org.pixel.commons.factory.Factory;

/**
 * Factory interface for creating platform-specific DirectRenderer instances.
 *
 * <p>DirectRenderer is typically used as a singleton for rendering objects
 * individually without batching when custom shaders are required.
 */
public interface DirectRendererFactory extends Factory {

    /**
     * Create a DirectRenderer instance.
     *
     * @return A new DirectRenderer instance appropriate for the current platform
     */
    DirectRenderer create();
}
