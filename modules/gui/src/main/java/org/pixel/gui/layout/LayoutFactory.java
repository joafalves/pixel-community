/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.layout;

import org.pixel.gui.component.UIContainer;
import org.pixel.gui.layout.yoga.YogaLayoutHandler;

public class LayoutFactory {

    /**
     * Create layout context
     *
     * @return
     */
    public static LayoutHandler createHandler(UIContainer container) {
        // for now, we only support yoga layout:
        return new YogaLayoutHandler(container);
    }
}
