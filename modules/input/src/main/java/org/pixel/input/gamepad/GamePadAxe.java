/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.gamepad;

import static org.lwjgl.glfw.GLFW.*;

public enum GamePadAxe {
    AXIS_LEFT_X(GLFW_GAMEPAD_AXIS_LEFT_X),
    AXIS_LEFT_Y(GLFW_GAMEPAD_AXIS_LEFT_Y),
    AXIS_RIGHT_X(GLFW_GAMEPAD_AXIS_RIGHT_X),
    AXIS_RIGHT_Y(GLFW_GAMEPAD_AXIS_RIGHT_Y),
    TRIGGER_LEFT(GLFW_GAMEPAD_AXIS_LEFT_TRIGGER),
    TRIGGER_RIGHT(GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER);

    private final int value;

    /**
     * Constructor
     *
     * @param value
     */
    GamePadAxe(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
