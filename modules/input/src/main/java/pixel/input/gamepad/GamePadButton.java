/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.input.gamepad;

import static org.lwjgl.glfw.GLFW.*;

public enum GamePadButton {
    A(GLFW_GAMEPAD_BUTTON_A),
    B(GLFW_GAMEPAD_BUTTON_B),
    X(GLFW_GAMEPAD_BUTTON_X),
    Y(GLFW_GAMEPAD_BUTTON_Y),
    CROSS(GLFW_GAMEPAD_BUTTON_A),
    CIRCLE(GLFW_GAMEPAD_BUTTON_B),
    SQUARE(GLFW_GAMEPAD_BUTTON_X),
    TRIANGLE(GLFW_GAMEPAD_BUTTON_Y),
    LEFT_BUMPER(GLFW_GAMEPAD_BUTTON_LEFT_BUMPER),
    RIGHT_BUMPER(GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER),
    BACK(GLFW_GAMEPAD_BUTTON_BACK),
    START(GLFW_GAMEPAD_BUTTON_START),
    GUIDE(GLFW_GAMEPAD_BUTTON_GUIDE),
    LEFT_STICK(GLFW_GAMEPAD_BUTTON_LEFT_THUMB),
    RIGHT_STICK(GLFW_GAMEPAD_BUTTON_RIGHT_THUMB),
    DPAD_UP(GLFW_GAMEPAD_BUTTON_DPAD_UP),
    DPAD_RIGHT(GLFW_GAMEPAD_BUTTON_DPAD_RIGHT),
    DPAD_DOWN(GLFW_GAMEPAD_BUTTON_DPAD_DOWN),
    DPAD_LEFT(GLFW_GAMEPAD_BUTTON_DPAD_LEFT);

    private int value;

    /**
     * Constructor
     *
     * @param value
     */
    GamePadButton(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
