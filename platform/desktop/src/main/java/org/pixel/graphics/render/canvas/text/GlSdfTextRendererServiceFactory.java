package org.pixel.graphics.render.canvas.text;

import org.pixel.commons.service.ServiceFactory;
import org.pixel.graphics.render.SdfTextRenderer;

public class GlSdfTextRendererServiceFactory implements ServiceFactory<SdfTextRenderer> {

    @Override
    public SdfTextRenderer get() {
        return new GlSdfTextRenderer();
    }

}
