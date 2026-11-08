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
        position = new Vector2();
        virtualPosition = new Vector2();
        positionBox = new Rectangle();
    }

    //endregion

    //region properties

    private static final HashMap<Integer, Integer> buttons;
    private static final Vector2 position;
    private static final Vector2 virtualPosition;
    private static volatile float virtualScaleX = 1f;
    private static volatile float virtualScaleY = 1f;
    private static final Rectangle positionBox;
    private static volatile float scrollX, scrollY; // wheel/trackpad scrolling since the last frame

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
    /**
     * Get the current mouse position in virtual (game resolution) coordinates, i.e. the same space used by the
     * camera and sprites regardless of the actual window size or display DPI. Use this with
     * {@code Camera2D.screenToVirtualCoordinates}.
     *
     * @return A copy of the mouse position in virtual coordinates.
     */
    public static Vector2 getVirtualPosition() {
        return new Vector2(virtualPosition);
    }

    /**
     * Copy the current mouse position in virtual (game resolution) coordinates to the given vector.
     *
     * @param destination The destination vector.
     */
    public static void getVirtualPosition(Vector2 destination) {
        destination.set(virtualPosition);
    }

    /**
     * Get the current mouse position X in virtual (game resolution) coordinates.
     *
     * @return The mouse position X in virtual coordinates.
     */
    public static float getVirtualX() {
        return virtualPosition.getX();
    }

    /**
     * Get the current mouse position Y in virtual (game resolution) coordinates.
     *
     * @return The mouse position Y in virtual coordinates.
     */
    public static float getVirtualY() {
        return virtualPosition.getY();
    }

    /**
     * @return Vertical scrolling (mouse wheel or trackpad) during the last frame: positive away from the user ("up"),
     * in wheel steps (trackpads report fractions).
     */
    public static float getScrollY() {
        return scrollY;
    }

    /**
     * @return Horizontal scrolling during the last frame (positive to the right).
     */
    public static float getScrollX() {
        return scrollX;
    }

    /**
     * Forget the scrolling of the frame that ended. Called by the window manager at the end of each frame; not meant
     * to be called by game code.
     */
    public static void clearScroll() {
        scrollX = 0f;
        scrollY = 0f;
    }

    /**
     * Set the factor between window (screen) coordinates and virtual coordinates. Called by the window manager
     * whenever the window size changes; not meant to be called by game code.
     *
     * @param scaleX virtualWidth / windowWidth (in screen coordinates).
     * @param scaleY virtualHeight / windowHeight (in screen coordinates).
     */
    public static void setVirtualScale(float scaleX, float scaleY) {
        virtualScaleX = scaleX;
        virtualScaleY = scaleY;
        virtualPosition.set(position.getX() * scaleX, position.getY() * scaleY);
    }

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

    //endregion

    //region internal classes

    /**
     * Cursor Position Handler.
     */
    public static class CursorPositionHandler extends GLFWCursorPosCallback {

        @Override
        public void invoke(long window, double x, double y) {
            position.set((float) x, (float) y);
            virtualPosition.set((float) x * virtualScaleX, (float) y * virtualScaleY);
            positionBox.set((float) x, (float) y, 1, 1);
        }
    }

    /**
     * Scroll Handler: adds up the scrolling until the frame ends.
     */
    public static class ScrollHandler extends GLFWScrollCallback {

        @Override
        public void invoke(long window, double x, double y) {
            scrollX += (float) x;
            scrollY += (float) y;
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
