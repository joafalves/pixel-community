/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.pixel.graphics.render.RenderEngine2D;
import org.pixel.core.WindowDimensions;
import org.pixel.gui.style.Style;

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
