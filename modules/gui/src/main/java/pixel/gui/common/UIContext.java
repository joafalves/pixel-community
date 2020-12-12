/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import pixel.core.WindowDimensions;
import pixel.graphics.render.RenderEngine2D;
import pixel.gui.style.Style;

@Builder
@Getter
@Setter
public class UIContext {

    private RenderEngine2D renderEngine;
    private WindowDimensions windowDimensions;
    private Style style;

    /**
     * @return
     */
    public int getViewportWidth() {
        return windowDimensions.getVirtualWidth();
    }

    /**
     * @return
     */
    public int getViewportHeight() {
        return windowDimensions.getVirtualHeight();
    }

}
