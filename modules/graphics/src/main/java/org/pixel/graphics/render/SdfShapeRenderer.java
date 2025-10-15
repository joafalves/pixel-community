package org.pixel.graphics.render;

import org.pixel.commons.Color;
import org.pixel.math.Matrix4;

/**
 * Renderer interface for SDF (Signed Distance Field) based shapes.
 * Provides efficient shape rendering using GPU fragment shaders.
 * Only requires 2 triangles (1 quad) per shape regardless of complexity.
 */
public interface SdfShapeRenderer extends Renderer {
    
    /**
     * Render a filled rounded rectangle using SDF.
     *
     * @param x          X position
     * @param y          Y position
     * @param width      Rectangle width
     * @param height     Rectangle height
     * @param radius     Corner radius (0 = sharp corners)
     * @param color      Fill color
     * @param transform  Transform matrix (combines local transform + view matrix)
     */
    void fillRoundedRect(float x, float y, float width, float height, float radius, Color color, Matrix4 transform);
    
    /**
     * Render a stroked rounded rectangle using SDF.
     *
     * @param x           X position
     * @param y           Y position
     * @param width       Rectangle width
     * @param height      Rectangle height
     * @param radius      Corner radius (0 = sharp corners)
     * @param strokeWidth Stroke width
     * @param color       Stroke color
     * @param transform   Transform matrix
     */
    void strokeRoundedRect(float x, float y, float width, float height, float radius, float strokeWidth, Color color, Matrix4 transform);
    
    /**
     * Render a filled circle using SDF.
     *
     * @param x         X position (center)
     * @param y         Y position (center)
     * @param radius    Circle radius
     * @param color     Fill color
     * @param transform Transform matrix
     */
    void fillCircle(float x, float y, float radius, Color color, Matrix4 transform);
    
    /**
     * Render a stroked circle using SDF.
     *
     * @param x           X position (center)
     * @param y           Y position (center)
     * @param radius      Circle radius
     * @param strokeWidth Stroke width
     * @param color       Stroke color
     * @param transform   Transform matrix
     */
    void strokeCircle(float x, float y, float radius, float strokeWidth, Color color, Matrix4 transform);
}
