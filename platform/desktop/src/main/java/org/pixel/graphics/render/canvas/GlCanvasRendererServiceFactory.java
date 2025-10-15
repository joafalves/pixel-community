package org.pixel.graphics.render.canvas;

import org.pixel.commons.service.ServiceFactory;

public class GlCanvasRendererServiceFactory implements ServiceFactory<CanvasRenderer> {

    @Override
    public CanvasRenderer get() {
        // Create CanvasRenderer with a sensible default viewport size
        // The actual view matrix will be provided in begin() calls
        // Default is 1920x1080 for screen-space rendering
        return new GlCanvasRenderer(1920, 1080);
    }

}
