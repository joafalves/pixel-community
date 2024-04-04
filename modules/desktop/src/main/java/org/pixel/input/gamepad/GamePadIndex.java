/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.gamepad;

import org.lwjgl.glfw.GLFW;

public enum GamePadIndex {
    P1(GLFW.GLFW_JOYSTICK_1),
    P2(GLFW.GLFW_JOYSTICK_2),
    P3(GLFW.GLFW_JOYSTICK_3),
    P4(GLFW.GLFW_JOYSTICK_4),
    P5(GLFW.GLFW_JOYSTICK_5),
    P6(GLFW.GLFW_JOYSTICK_6),
    P7(GLFW.GLFW_JOYSTICK_7),
    P8(GLFW.GLFW_JOYSTICK_8),
    P9(GLFW.GLFW_JOYSTICK_9),
    P10(GLFW.GLFW_JOYSTICK_10),
    P11(GLFW.GLFW_JOYSTICK_11),
    P12(GLFW.GLFW_JOYSTICK_12),
    P13(GLFW.GLFW_JOYSTICK_13),
    P14(GLFW.GLFW_JOYSTICK_14),
    P15(GLFW.GLFW_JOYSTICK_15),
    P16(GLFW.GLFW_JOYSTICK_16);

    private final int value;

    /**
     * Constructor.
     *
     * @param value The index value.
     */
    GamePadIndex(int value) {
        this.value = value;
    }

    /**
     * Get index value.
     *
     * @return The index value.
     */
    public int getValue() {
        return this.value;
    }
}
