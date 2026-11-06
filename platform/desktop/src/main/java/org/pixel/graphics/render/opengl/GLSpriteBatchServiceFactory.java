package org.pixel.graphics.render.opengl;

import org.pixel.commons.service.ServiceFactory;
import org.pixel.graphics.RenderStats;
import org.pixel.graphics.render.SpriteBatch;

/**
 * Creates {@link GLSpriteBatch} instances that record their GPU work into the device's {@link RenderStats}.
 */
public class GLSpriteBatchServiceFactory implements ServiceFactory<SpriteBatch> {

    private final RenderStats stats;

    /**
     * Constructor.
     *
     * @param stats The render statistics of the graphics device.
     */
    public GLSpriteBatchServiceFactory(RenderStats stats) {
        if (stats == null) {
            throw new IllegalArgumentException("Render stats cannot be null.");
        }
        this.stats = stats;
    }

    @Override
    public SpriteBatch get() {
        final var spriteBatch = new GLSpriteBatch(stats);
        if (!spriteBatch.init()) {
            throw new RuntimeException("Failed to initialize GLSpriteBatch.");
        }
        return spriteBatch;
    }
}
