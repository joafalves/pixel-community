package org.pixel.graphics.render;

import org.pixel.math.Matrix4;

/**
 * Base interface for all renderer types in the rendering pipeline.
 * Specific renderer implementations extend this interface to provide specialized rendering.
 * 
 * <p>Two main categories:
 * <ul>
 *   <li>{@link BatchRenderer} - Accumulates draw calls and flushes them together (e.g., SpriteBatch)</li>
 *   <li>{@link Renderer} - Renders immediately without batching (e.g., SdfShapeRenderer)</li>
 * </ul>
 */
public interface Renderer {
    // Base marker interface - direct renderers implement this directly
}

/**
 * Interface for renderers that support batching.
 * Batched renderers accumulate draw calls between begin() and end(),
 * then flush them all at once for optimal performance.
 */
interface BatchRenderer extends Renderer {
    /**
     * Begin a batched rendering session.
     * 
     * @param viewMatrix The camera's view-projection matrix
     */
    void begin(Matrix4 viewMatrix);
    
    /**
     * End the batched rendering session and flush all accumulated draw calls.
     */
    void end();
}
