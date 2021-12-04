/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.mouse;

import static org.lwjgl.glfw.GLFW.*;

public enum MouseButton {

    B1(GLFW_MOUSE_BUTTON_1),
    B2(GLFW_MOUSE_BUTTON_2),
    B3(GLFW_MOUSE_BUTTON_3),
    B4(GLFW_MOUSE_BUTTON_4),
    B5(GLFW_MOUSE_BUTTON_5),
    B6(GLFW_MOUSE_BUTTON_6),
    B7(GLFW_MOUSE_BUTTON_7),
    B8(GLFW_MOUSE_BUTTON_8),

    LAST(GLFW_MOUSE_BUTTON_LAST),
    LEFT(GLFW_MOUSE_BUTTON_LEFT),
    MIDDLE(GLFW_MOUSE_BUTTON_MIDDLE),
    RIGHT(GLFW_MOUSE_BUTTON_RIGHT);

    private final int value;

    /**
     * Constructor
     *
     * @param value
     */
    MouseButton(int value) {
        this.value = value;
    }

    /**
     * Get mouse button value
     *
     * @return
     */
    public int getValue() {
        return this.value;
    }
}
