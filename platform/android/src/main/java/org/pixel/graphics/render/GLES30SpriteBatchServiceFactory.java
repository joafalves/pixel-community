package org.pixel.graphics.render;

import org.pixel.commons.service.ServiceFactory;

public class GLES30SpriteBatchServiceFactory implements ServiceFactory<SpriteBatch> {

    @Override
    public SpriteBatch get() {
        GLES30SpriteBatch spriteBatch = new GLES30SpriteBatch();
        if (!spriteBatch.init()) {
            throw new RuntimeException("Failed to initialize GLES30SpriteBatch.");
        }
        return spriteBatch;
    }

}
