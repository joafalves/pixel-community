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

    private static final HashMap<Integer, Integer> buttons;
    private static final Vector2 position;
    private static final Rectangle positionBox;

    //endregion

    //region public static methods

    /**
     * Static method to get a snapshot of the mouse state.
     *
     * @return MouseState snapshot.
     */
    public static MouseState getState() {
        return Mouse.getMouseState();
    }

    //endregion

    //region public methods

    /**
     * Static method to determine if the given mouse button is down.
     *
     * @param button MouseButton to check.
     * @return True if the button is down, false otherwise.
     */
    public static boolean isMouseButtonDown(MouseButton button) {
        Integer value = buttons.get(button.getValue());
        return value != null && value > 0;
    }

    /**
     * Static method to determine if the given mouse button is up.
     *
     * @param button MouseButton to check.
     * @return True if the button is up, false otherwise.
     */
    public static boolean isMouseButtonUp(MouseButton button) {
        Integer value = buttons.get(button.getValue());
        return value != null && value == 0;
    }

    /**
     * Static method to get a snapshot of the mouse state.
     *
     * @return MouseState snapshot.
     */
    public static MouseState getMouseState() {
        return new MouseState((HashMap<Integer, Integer>) buttons.clone());
    }

    //endregion

    //region getters & setters

    /**
     * Get the current mouse position.
     *
     * @return The current mouse position.
     */
    public static Vector2 getPosition() {
        return position;
    }

    /**
     * Get the current mouse position box.
     *
     * @return The current mouse position box.
     */
    public static Rectangle getPositionBox() {
        return positionBox;
    }

    //endregion

    //region internal classes

    /**
     * Cursor Position Handler.
     */
    public static class CursorPositionHandler extends GLFWCursorPosCallback {

        @Override
        public void invoke(long window, double x, double y) {
            position.set((float) x, (float) y);
            positionBox.set((float) x, (float) y, 1, 1);
        }
    }

    /**
     * Mouse Button Handler.
     */
    public static class MouseButtonHandler extends GLFWMouseButtonCallback {

        @Override
        public void invoke(long window, int button, int action, int mods) {
            buttons.put(button, action);
        }
    }

    //endregion
}
