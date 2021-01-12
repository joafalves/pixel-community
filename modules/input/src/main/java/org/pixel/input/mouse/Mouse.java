/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.mouse;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.util.HashMap;

public class Mouse {

    //region singleton

    static {
        buttons = new HashMap<>();
        position = new Vector2();
        positionBox = new Rectangle();
    }

    //endregion

    //region properties

    private static HashMap<Integer, Integer> buttons;
    private static Vector2 position;
    private static Rectangle positionBox;

    //endregion

    //region public static methods

    public static MouseState getState() {
        return Mouse.getMouseState();
    }

    //endregion

    //region public methods

    public static boolean isMouseButtonDown(MouseButton button) {
        Integer value = buttons.get(button.getValue());
        return value != null && value > 0;
    }

    public static boolean isMouseButtonUp(MouseButton button) {
        Integer value = buttons.get(button.getValue());
        return value != null && value == 0;
    }

    /**
     * @return
     */
    public static MouseState getMouseState() {
        return new MouseState((HashMap<Integer, Integer>) buttons.clone());
    }

    //endregion

    //region private methods


    //endregion

    //region getters & setters

    /**
     * @return
     */
    public static Vector2 getPosition() {
        return position;
    }

    /**
     * @return
     */
    public static Rectangle getPositionBox() {
        return positionBox;
    }

    //endregion

    //region internal classes

    /**
     * Cursor Position Handler
     */
    public static class CursorPositionHandler extends GLFWCursorPosCallback {

        @Override
        public void invoke(long window, double x, double y) {
            position.set((float) x, (float) y);
            positionBox.set((float) x, (float) y, 1, 1);
        }
    }

    /**
     * Mouse Button Handler
     */
    public static class MouseButtonHandler extends GLFWMouseButtonCallback {

        @Override
        public void invoke(long window, int button, int action, int mods) {
            buttons.put(button, action);
        }
    }

    //endregion
}
