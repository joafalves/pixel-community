package org.pixel.ext.weaver;

import lombok.Builder;
import lombok.Getter;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.ext.weaver.style.resource.ResourceStore;
import org.pixel.graphics.render.canvas.Canvas;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.math.Rectangle;

@Builder
public class WeaverContext implements Disposable {
    @Getter
    private Canvas canvas;
    @Getter
    private StyleEngine styleEngine;

    @Getter
    private LayoutEngine layoutEngine;

    @Getter
    private ResourceStore<SdfFont> fontStore;

    @Getter
    private Rectangle viewport;

    @Override
    public void dispose() {
        if (canvas != null) {
            canvas.dispose();
        }
    }

    public void setViewport(int width, int height) {
        viewport.set(0, 0, width, height);
        if (canvas != null) {
            canvas.setViewport(width, height);
        }
    }
}
