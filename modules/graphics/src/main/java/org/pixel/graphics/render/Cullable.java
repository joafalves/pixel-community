package org.pixel.graphics.render;

import org.pixel.math.Rectangle;

/**
 * Interface for objects that can be culled (visibility tested) against a view frustum.
 * Implementing this interface allows renderables to provide their bounding box for frustum culling.
 */
public interface Cullable {
    
    /**
     * Gets the world-space bounding box of this object for culling purposes.
     * The bounding box should encompass all visible pixels of the object.
     * 
     * @return The world-space bounding rectangle, or null if this object should never be culled.
     */
    Rectangle getBounds();
}
