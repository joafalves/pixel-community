package org.pixel.graphics.render;

import org.pixel.commons.ServiceFactory;

public class GLES30SpriteBatchServiceFactory implements ServiceFactory<SpriteBatch> {

    @Override
    public SpriteBatch create() {
        GLES30SpriteBatch spriteBatch = new GLES30SpriteBatch();
        if (!spriteBatch.init()) {
            throw new RuntimeException("Failed to initialize GLES30SpriteBatch.");
        }
        return spriteBatch;
    }

}
