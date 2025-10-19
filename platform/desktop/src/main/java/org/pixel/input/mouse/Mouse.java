/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.mouse;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.util.HashMap;

public class Mouse {

    //region singleton

    static {
        buttons = new HashMap<>();
        pressedButtons = new HashMap<>();
        position = new Vector2();
        positionBox = new Rectangle();
        wheelDelta = 0f;
    }

    //endregion

    //region properties

    // NOTE: the pressed buttons exist because it's much faster to consider that only
    // pressed buttons exist on that map than having to manually loop through the buttons
    // map to check if the button is pressed or not (end of frame clean).

    private static final HashMap<Integer, Integer> pressedButtons; // <Button, Action>
    private static final HashMap<Integer, Integer> buttons; // <Button, Action>
    private static final Vector2 position;
    private static final Rectangle positionBox;
    private static float wheelDelta;

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
     * Static method to determine if the given mouse button was just pressed this frame.
     * Returns true only on the frame when the button transitions from up to down.
     *
     * @param button MouseButton to check.
     * @return True if the button was just pressed, false otherwise.
     */
    public static boolean isMouseButtonPressed(MouseButton button) {
        Integer value = pressedButtons.get(button.getValue());
        return value != null && value == 1; // GLFW_PRESS = 1
    }

    /**
     * Static method to clear all single-frame mapped buttons. This function shall be
     * called once at the end of the render frame (before native event polling).
     */
    public static void clear() {
        pressedButtons.clear();
    }

    /**
     * Static method to get a snapshot of the mouse state.
     *
     * @return MouseState snapshot.
     */
    public static MouseState getMouseState() {
        return new MouseState(new HashMap<>(buttons));
    }

    //endregion

    //region getters & setters

    /**
     * Get the current mouse position X.
     *
     * @return The current mouse position X.
     */
    public static float getX() {
        return position.getX();
    }

    /**
     * Get the current mouse position Y.
     *
     * @return The current mouse position Y.
     */
    public static float getY() {
        return position.getY();
    }

    /**
     * Get the current mouse position.
     *
     * @return The current mouse position.
     */
    public static Vector2 getPosition() {
        return new Vector2(position);
    }

    /**
     * Get the current mouse position.
     *
     * @param destination Destination vector to store the result.
     */
    public static void getPosition(Vector2 destination) {
        destination.set(position);
    }

    /**
     * Get the current mouse position box.
     *
     * @return The current mouse position box.
     */
    public static Rectangle getPositionBox() {
        return new Rectangle(positionBox);
    }

    /**
     * Get the current mouse position box.
     *
     * @param destination Destination rectangle to store the result.
     */
    public static void getPositionBox(Rectangle destination) {
        destination.set(positionBox);
    }

    /**
     * Get the mouse wheel delta for this frame.
     * Positive values indicate scrolling up, negative values indicate scrolling down.
     *
     * @return The mouse wheel delta.
     */
    public static float getWheelDelta() {
        return wheelDelta;
    }

    /**
     * Reset the mouse wheel delta.
     * This should be called at the end of each frame to clear the delta for the next frame.
     */
    public static void resetWheelDelta() {
        wheelDelta = 0f;
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
            if (action == 1) { // GLFW_PRESS = 1
                pressedButtons.put(button, action);
            }
        }
    }

    /**
     * Mouse Scroll Handler.
     */
    public static class MouseScrollHandler extends GLFWScrollCallback {

        @Override
        public void invoke(long window, double xOffset, double yOffset) {
            wheelDelta = (float) yOffset;
        }
    }

    //endregion
}
