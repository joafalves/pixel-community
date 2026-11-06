package org.pixel.graphics.render;

import org.pixel.commons.service.ServiceFactory;
import org.pixel.graphics.backend.RenderBackend;

/**
 * Creates {@link DrawListSpriteBatch} instances on top of the device's render backend.
 */
public class DrawListSpriteBatchServiceFactory implements ServiceFactory<SpriteBatch> {

    private final RenderBackend backend;

    /**
     * Constructor.
     *
     * @param backend The render backend of the graphics device.
     */
    public DrawListSpriteBatchServiceFactory(RenderBackend backend) {
        if (backend == null) {
            throw new IllegalArgumentException("Render backend cannot be null.");
        }
        this.backend = backend;
    }

    @Override
    public SpriteBatch get() {
        var spriteBatch = new DrawListSpriteBatch(backend);
        spriteBatch.init();
        return spriteBatch;
    }
}
