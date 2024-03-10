/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.gamepad;

import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class GamePad {

    /**
     * Checks if a given GamePad is connected.
     *
     * @param index The index of the GamePad to check.
     * @return True if the GamePad is connected, false otherwise.
     */
    public static boolean isConnected(GamePadIndex index) {
        return GLFW.glfwGetJoystickName(index.getValue()) != null;
    }

    /**
     * Get the name of a GamePad.
     *
     * @param index The index of the GamePad to get the name of.
     * @return The name of the GamePad or null if the GamePad is not connected.
     */
    public static String getName(GamePadIndex index) {
        return GLFW.glfwGetJoystickName(index.getValue());
    }

    /**
     * Get current GamePad state.
     *
     * @param index The index of the GamePad to get the state of.
     * @return The current GamePad state or null if the GamePad is not connected.
     */
    public static GamePadState getState(GamePadIndex index) {
        String name = getName(index);
        if (name != null) { // also checks if the controller is connected
            FloatBuffer axes = GLFW.glfwGetJoystickAxes(index.getValue());
            ByteBuffer buttons = GLFW.glfwGetJoystickButtons(index.getValue());

            return new GamePadState(name, buttons, axes);
        }

        return null;
    }

}
