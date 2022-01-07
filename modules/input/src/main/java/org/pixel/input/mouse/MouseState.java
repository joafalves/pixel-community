/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.mouse;

import java.io.Serializable;
import java.util.HashMap;

public class MouseState implements Serializable {

    //region properties

    private final HashMap<Integer, Integer> buttons;

    //endregion

    //region constructors

    /**
     * Constructor.
     *
     * @param buttons The snapshot of the mouse state.
     */
    public MouseState(HashMap<Integer, Integer> buttons) {
        this.buttons = buttons;
    }

    //endregion

    //region public methods

    /**
     * Checks if a given mouse button is down.
     *
     * @param button The mouse button to check.
     * @return True if the mouse button is down, false otherwise.
     */
    public boolean isMouseButtonDown(MouseButton button) {
        Integer value = buttons.get(button.getValue());
        return value != null && value > 0;
    }

    /**
     * Checks if a given mouse button is up.
     *
     * @param button The mouse button to check.
     * @return True if the mouse button is up, false otherwise.
     */
    public boolean isMouseButtonUp(MouseButton button) {
        Integer value = buttons.get(button.getValue());
        return value != null && value == 0;
    }

    //endregion


}
