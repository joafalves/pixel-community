package org.pixel.gui.event;

import org.pixel.gui.model.ComponentState;

public interface UIStateChangeListener extends UIEventListener {
    void onStateChange(ComponentState newState, ComponentState oldState);
}
