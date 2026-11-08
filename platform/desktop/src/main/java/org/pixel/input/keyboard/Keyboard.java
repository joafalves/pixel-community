/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.keyboard;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

public class Keyboard {

    // region singleton

    static {
        charListeners = new ArrayList<>();
    }

    // endregion

    // region properties

    // Indexed by GLFW key code: queried every frame, so no maps (boxing key codes above 127 allocates). The keys
    // pressed this frame are also listed, so clearing them at the end of the frame touches only those.
    private static final int NONE = -1; // no event for the key yet
    private static final int[] actions = noActions(); // last action per key
    private static final boolean[] pressed = new boolean[GLFW_KEY_LAST + 1]; // pressed this frame
    private static final int[] pressedList = new int[GLFW_KEY_LAST + 1];
    private static int pressedCount;
    private static final List<KeyboardCharListener> charListeners;

    // endregion

    // region public methods

    /**
     * Static method to determine if a key is down.
     *
     * @param key The key to check.
     * @return True if the key is down.
     */
    public static boolean isKeyDown(KeyboardKey key) {
        int action = action(key.getValue());
        return action == GLFW_PRESS || action == GLFW_REPEAT;
    }

    /**
     * Static method to determine if a key is up.
     *
     * @param key The key to check.
     * @return True if the key is up.
     */
    public static boolean isKeyUp(KeyboardKey key) {
        return action(key.getValue()) == GLFW_RELEASE;
    }

    /**
     * Static method to determine if a key is pressed.
     *
     * @param key The key to check.
     * @return True if the key is pressed.
     */
    public static boolean isKeyPressed(KeyboardKey key) {
        int code = key.getValue();
        return code >= 0 && code < pressed.length && pressed[code];
    }

    private static int[] noActions() {
        int[] array = new int[GLFW_KEY_LAST + 1];
        java.util.Arrays.fill(array, NONE);
        return array;
    }

    private static int action(int code) {
        return code >= 0 && code < actions.length ? actions[code] : NONE;
    }

    /**
     * Static method to get the current keyboard state.
     *
     * @return The current keyboard state.
     */
    public static KeyboardState getState() {
        HashMap<Integer, Integer> keys = new HashMap<>(), pressedKeys = new HashMap<>();
        for (int code = 0; code < actions.length; code++) {
            if (actions[code] != NONE) {
                keys.put(code, actions[code]);
            }
            if (pressed[code]) {
                pressedKeys.put(code, GLFW_PRESS);
            }
        }
        return new KeyboardState(keys, pressedKeys);
    }

    /**
     * Static method to add a keyboard char listener.
     *
     * @param listener The listener to add.
     */
    public static void addCharListener(KeyboardCharListener listener) {
        if (!charListeners.contains(listener)) {
            charListeners.add(listener);
        }
    }

    /**
     * Static method to remove a keyboard char listener.
     *
     * @param listener The listener to remove.
     * @return True if the listener was removed.
     */
    public static boolean removeCharListener(KeyboardCharListener listener) {
        return charListeners.remove(listener);
    }

    /**
     * Static method to clear all single-frame mapped keys. This function shall be
     * called once at the end of the render
     * frame (before native event polling).
     */
    public static void clear() {
        for (int i = 0; i < pressedCount; i++) {
            pressed[pressedList[i]] = false;
        }
        pressedCount = 0;
    }

    // endregion

    // region internal classes

    public static class KeyboardInputHandler extends GLFWKeyCallback {

        @Override
        public void invoke(long window, int key, int scanCode, int action, int mods) {
            if (key < 0 || key >= actions.length) {
                return; // GLFW_KEY_UNKNOWN (keys without a code)
            }
            actions[key] = action;
            if (action == GLFW_PRESS && !pressed[key]) {
                pressed[key] = true;
                pressedList[pressedCount++] = key;
            }
        }

    }

    public static class KeyboardCharacterHandler extends GLFWCharCallback {

        @Override
        public void invoke(long window, int codepoint) {
            for (KeyboardCharListener listener : charListeners) {
                listener.onCharacter((char) codepoint);
            }
        }

    }

    // endregion
}
