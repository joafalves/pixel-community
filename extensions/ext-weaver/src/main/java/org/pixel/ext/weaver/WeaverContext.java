package org.pixel.ext.weaver;

import lombok.Builder;
import lombok.Data;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.graphics.render.canvas.Canvas;

@Builder
@Data
public class WeaverContext implements Disposable {
    private Canvas canvas;
    private int viewportWidth;
    private int viewportHeight;

    @Override
    public void dispose() {
        if (canvas != null) {
            canvas.dispose();
        }
    }
}
