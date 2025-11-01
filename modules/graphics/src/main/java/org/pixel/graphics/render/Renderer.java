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