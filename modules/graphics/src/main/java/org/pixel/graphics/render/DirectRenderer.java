package org.pixel.graphics.render;

import org.pixel.commons.factory.FactoryProvider;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.lifecycle.Initializable;
import org.pixel.math.Matrix4;

/**
 * Defines the contract for a renderer that draws objects individually, without batching.
 * Used for sprites with custom shaders where batching is not possible.
 */
public interface DirectRenderer extends Renderer, Initializable, Disposable {

    /**
     * Create a platform-specific DirectRenderer instance.
     *
     * <p>This factory method delegates to the registered {@link DirectRendererFactory} to create
     * the appropriate platform-specific implementation (e.g., GLDirectRenderer on desktop).
     *
     * <p>Example usage:
     * <pre>
     * DirectRenderer renderer = DirectRenderer.create();
     * renderer.draw(renderable, viewMatrix);
     * renderer.dispose();
     * </pre>
     *
     * @return A new DirectRenderer instance appropriate for the current platform
     */
    static DirectRenderer create() {
        return FactoryProvider.get(DirectRendererFactory.class).create();
    }

    /**
     * Draws a single renderable with a custom shader.
     *
     * @param renderable The renderable to draw.
     * @param viewMatrix The camera's view-projection matrix.
     */
    void draw(Renderable<?> renderable, Matrix4 viewMatrix);
}
