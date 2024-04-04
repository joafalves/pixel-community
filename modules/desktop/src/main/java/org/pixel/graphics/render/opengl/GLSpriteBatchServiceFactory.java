package org.pixel.graphics.render.opengl;

import org.pixel.commons.ServiceFactory;
import org.pixel.graphics.render.SpriteBatch;

public class GLSpriteBatchServiceFactory implements ServiceFactory<SpriteBatch> {

    @Override
    public SpriteBatch create() {
        return new GLSpriteBatch();
    }

}
