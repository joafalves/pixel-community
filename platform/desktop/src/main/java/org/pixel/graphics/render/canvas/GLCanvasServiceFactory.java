package org.pixel.graphics.render.canvas;

/**
 * OpenGL implementation of the CanvasFactory for desktop platforms.
 */
public class GLCanvasServiceFactory implements CanvasFactory {

    @Override
    public Canvas create(int width, int height) {
        return new GLCanvas(width, height);
    }
}
