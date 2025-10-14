package org.pixel.graphics.render.opengl;

import org.pixel.commons.service.ServiceFactory;
import org.pixel.graphics.render.DirectRenderer;

public class GLDirectRendererServiceFactory implements ServiceFactory<DirectRenderer> {

    @Override
    public DirectRenderer get() {
        final var directRenderer = new GLDirectRenderer();
        if (!directRenderer.init()) {
            throw new RuntimeException("Failed to initialize GLDirectRenderer.");
        }
        return directRenderer;
    }

}
