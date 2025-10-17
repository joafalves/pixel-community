/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

/**
 * OpenGL implementation of the fluent Canvas API.
 * 
 * <p>This class wraps {@link GLCanvasRenderer} to provide a modern,
 * fluent interface for 2D drawing operations.
 * 
 * <p>Example usage:
 * <pre>
 * Canvas canvas = new GlCanvas(800, 600);
 * 
 * canvas.begin();
 * 
 * canvas.rect(100, 100, 200, 50)
 *     .withFill(Color.BLUE)
 *     .withRoundedCorners(8);
 * 
 * canvas.text("Hello World", font, 10, 10)
 *     .withFill(Color.WHITE);
 * 
 * canvas.end();
 * </pre>
 */
public class GLCanvas extends Canvas {

    /**
     * Create a new OpenGL Canvas with the specified viewport dimensions.
     * 
     * @param width  Viewport width
     * @param height Viewport height
     */
    public GLCanvas(int width, int height) {
        super(new GLCanvasRenderer(width, height));
    }
    
    /**
     * Get the underlying GlCanvasRenderer for advanced OpenGL-specific operations.
     * 
     * @return The underlying GlCanvasRenderer
     */
    public GLCanvasRenderer getGlRenderer() {
        return (GLCanvasRenderer) renderer;
    }
}
