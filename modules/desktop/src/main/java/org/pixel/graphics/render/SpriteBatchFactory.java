package org.pixel.graphics.render;

import org.pixel.graphics.GameWindow;
import org.pixel.graphics.GraphicsBackend;
import org.pixel.graphics.render.opengl.GLSpriteBatch;

public class SpriteBatchFactory {

    private static final int DEFAULT_CAPACITY = 256;

    /**
     * Create a new sprite batch instance considering the current graphics backend.
     * 
     * @param window - The game window instance.
     * @return - The sprite batch instance.
     */
    public static SpriteBatch create(GameWindow window) {
        return create(window, DEFAULT_CAPACITY);
    }

    /**
     * Create a new sprite batch instance considering the current graphics backend.
     * 
     * @param window   - The game window instance.
     * @param capacity - The initial capacity of the sprite batch.
     * @return - The sprite batch instance.
     */
    public static SpriteBatch create(GameWindow window, int capacity) {
        return create(window.getSettings().getGraphicsBackend(), capacity);
    }

    /**
     * Create a new sprite batch instance considering the current graphics backend.
     * 
     * @param graphicsBackend - The graphics backend instance.
     * @param capacity        - The initial capacity of the sprite batch.
     * @return - The sprite batch instance.
     */
    public static SpriteBatch create(GraphicsBackend graphicsBackend, int capacity) {
        switch (graphicsBackend) {
            case OpenGL -> {
                return new GLSpriteBatch(capacity);
            }
            default -> {
                throw new UnsupportedOperationException(
                        "Unsupported graphics backend: " + graphicsBackend);
            }
        }
    }
}
