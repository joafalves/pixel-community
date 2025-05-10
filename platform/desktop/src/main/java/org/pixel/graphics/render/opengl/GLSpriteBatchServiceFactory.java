package org.pixel.graphics.render.opengl;

import org.pixel.commons.service.ServiceFactory;
import org.pixel.graphics.render.SpriteBatch;

public class GLSpriteBatchServiceFactory implements ServiceFactory<SpriteBatch> {

    @Override
    public SpriteBatch get() {
        var spriteBatch = new GLSpriteBatch();
        if (!spriteBatch.init()) {
            throw new RuntimeException("Failed to initialize GLSpriteBatch.");
        }
        return spriteBatch;
    }

}
