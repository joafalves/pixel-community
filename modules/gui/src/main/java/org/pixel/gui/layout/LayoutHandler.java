/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.layout;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.gui.component.UIComponent;

public interface LayoutHandler extends Disposable {

    void reload();

    void componentAdded(UIComponent component);

    void componentRemoved(UIComponent component);

    void computeLayout();
}
