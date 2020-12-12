/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.input.gamepad;

import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class GamePad {

    /**
     * Checks if a given GamePad is connected
     *
     * @param index
     * @return
     */
    public static boolean isConnected(GamePadIndex index) {
        return GLFW.glfwGetJoystickName(index.getValue()) != null;
    }

    /**
     * Get current GamePad state
     *
     * @param index
     * @return
     */
    public static GamePadState getState(GamePadIndex index) {
        String name = GLFW.glfwGetJoystickName(index.getValue());
        if (name != null) {
            // the controller is connected
            FloatBuffer axes = GLFW.glfwGetJoystickAxes(index.getValue());
            ByteBuffer buttons = GLFW.glfwGetJoystickButtons(index.getValue());

            return new GamePadState(name, buttons, axes);
        }

        return null;
    }

}
