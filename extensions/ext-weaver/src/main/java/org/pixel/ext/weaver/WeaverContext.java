package org.pixel.ext.weaver;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.ext.weaver.layout.LayoutEngine;
import org.pixel.ext.weaver.style.StyleEngine;
import org.pixel.graphics.render.canvas.Canvas;

@Builder
public class WeaverContext implements Disposable {
    @Getter
    private Canvas canvas;
    @Getter
    private StyleEngine styleEngine;

    @Getter
    private LayoutEngine layoutEngine;

    @Getter
    @Setter
    private int viewportWidth;
    @Getter
    @Setter
    private int viewportHeight;

    @Override
    public void dispose() {
        if (canvas != null) {
            canvas.dispose();
        }
    }

    public void setViewport(int width, int height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        if (canvas != null) {
            canvas.setViewport(width, height);
        }
    }
}
