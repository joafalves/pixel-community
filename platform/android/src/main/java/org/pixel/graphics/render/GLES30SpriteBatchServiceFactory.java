package org.pixel.graphics.render;

import org.pixel.commons.ServiceFactory;

public class GLES30SpriteBatchServiceFactory implements ServiceFactory<SpriteBatch> {

    @Override
    public SpriteBatch create() {
        return new GLES30SpriteBatch();
    }

}
