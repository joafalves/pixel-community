/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.layout;

import pixel.commons.lifecycle.Disposable;
import pixel.gui.component.UIComponent;

public interface LayoutHandler extends Disposable {

    void reload();

    void componentAdded(UIComponent component);

    void componentRemoved(UIComponent component);

    void computeLayout();
}
