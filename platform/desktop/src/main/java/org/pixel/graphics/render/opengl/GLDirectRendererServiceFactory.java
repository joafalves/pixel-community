package org.pixel.graphics.render.opengl;

import org.pixel.graphics.render.DirectRenderer;
import org.pixel.graphics.render.DirectRendererFactory;

public class GLDirectRendererServiceFactory implements DirectRendererFactory {

    @Override
    public DirectRenderer create() {
        final var directRenderer = new GLDirectRenderer();
        if (!directRenderer.init()) {
            throw new RuntimeException("Failed to initialize GLDirectRenderer.");
        }
        return directRenderer;
    }

}
