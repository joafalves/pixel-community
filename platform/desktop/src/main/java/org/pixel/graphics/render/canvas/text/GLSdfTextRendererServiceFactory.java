package org.pixel.graphics.render.canvas.text;

import org.pixel.graphics.render.SdfTextRenderer;
import org.pixel.graphics.render.SdfTextRendererFactory;

public class GLSdfTextRendererServiceFactory implements SdfTextRendererFactory {

    @Override
    public SdfTextRenderer create() {
        return new GLSdfTextRenderer();
    }

}
