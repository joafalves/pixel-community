/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.flux.core;

import org.pixel.math.Vector2;

/**
 * Manages viewport dimensions and coordinate systems for Flux GUI.
 * 
 * <p>Viewport is separate from input because it's a rendering/layout concern,
 * not an input concern. All platforms (desktop, mobile, web) have viewports.
 * 
 * <p>Responsibilities:
 * <ul>
 *   <li>Track viewport width and height</li>
 *   <li>Handle viewport resize events</li>
 *   <li>Provide coordinate conversion utilities (future)</li>
 * </ul>
 * 
 * @see org.pixel.ext.flux.Flux#getViewport()
 */
public class FluxViewport {
    
    private int width;
    private int height;
    
    /**
     * Create a viewport with specified dimensions.
     * 
     * @param width  Initial viewport width in pixels
     * @param height Initial viewport height in pixels
     */
    public FluxViewport(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Viewport dimensions must be positive: " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
    }
    
    /**
     * Get the current viewport width.
     * 
     * @return Viewport width in pixels
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Get the current viewport height.
     * 
     * @return Viewport height in pixels
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Resize the viewport to new dimensions.
     * Called when the window or screen is resized.
     * 
     * @param width  New viewport width in pixels
     * @param height New viewport height in pixels
     */
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Viewport dimensions must be positive: " + width + "x" + height);
        }
        this.width = width;
        this.height = height;
    }
    
    /**
     * Get viewport dimensions as a vector.
     * 
     * @return Vector2 with (width, height)
     */
    public Vector2 getDimensions() {
        return new Vector2(width, height);
    }
    
    /**
     * Get viewport aspect ratio (width / height).
     * 
     * @return Aspect ratio
     */
    public float getAspectRatio() {
        return (float) width / height;
    }
    
    /**
     * Check if a point is within the viewport bounds.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @return True if point is inside viewport
     */
    public boolean contains(float x, float y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    @Override
    public String toString() {
        return "FluxViewport{" + width + "x" + height + "}";
    }
}
