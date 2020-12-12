/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.layout;

import pixel.gui.component.UIContainer;
import pixel.gui.layout.yoga.YogaLayoutHandler;

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
