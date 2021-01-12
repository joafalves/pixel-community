/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.mouse;

import java.util.HashMap;

public class MouseState {

    //region properties

    private HashMap<Integer, Integer> buttons;

    //endregion

    //region constructors

    /**
     * Constructor
     *
     * @param buttons
     */
    public MouseState(HashMap<Integer, Integer> buttons) {
        this.buttons = buttons;
    }

    //endregion

    //region public methods

    /**
     * Checks if a given mouse button is down
     *
     * @param button
     * @return
     */
    public boolean isMouseButtonDown(MouseButton button) {
        Integer value = buttons.get(button.getValue());
        return value != null && value > 0;
    }

    /**
     * Checks if a given mouse button is up
     *
     * @param button
     * @return
     */
    public boolean isMouseButtonUp(MouseButton button) {
        Integer value = buttons.get(button.getValue());
        return value != null && value == 0;
    }

    //endregion


}
