/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.keyboard;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

public class Keyboard {

    //region singleton

    static {
        keys = new HashMap<>();
        pressedKeys = new HashMap<>();
        charListeners = new ArrayList<>();
    }

    //endregion

    //region properties

    // NOTE: the pressed keys exist because it's must faster to consider that only pressed keys exist on that map
    // than having to manually loop through the keys map to check if the key is pressed or not (end of frame clean).

    private static final HashMap<Integer, Integer> pressedKeys;         // <Key, Action>
    private static final HashMap<Integer, Integer> keys;                // <Key, Action>
    private static final List<KeyboardCharListener> charListeners;

    //endregion

    //region public methods

    /**
     * Static method to determine if a key is down.
     *
     * @param key The key to check.
     * @return True if the key is down.
     */
    public static boolean isKeyDown(KeyboardKey key) {
        var value = keys.get(key.getValue());
        return value != null && (value == GLFW_PRESS || value == GLFW_REPEAT);
    }

    /**
     * Static method to determine if a key is up.
     *
     * @param key The key to check.
     * @return True if the key is up.
     */
    public static boolean isKeyUp(KeyboardKey key) {
        var value = keys.get(key.getValue());
        return value != null && value == GLFW_RELEASE;
    }

    /**
     * Static method to determine if a key is pressed.
     *
     * @param key The key to check.
     * @return True if the key is pressed.
     */
    public static boolean isKeyPressed(KeyboardKey key) {
        var value = pressedKeys.get(key.getValue());
        return (value != null && value == GLFW_PRESS);
    }

    /**
     * Static method to get the current keyboard state.
     *
     * @return The current keyboard state.
     */
    public static KeyboardState getState() {
        return new KeyboardState((HashMap<Integer, Integer>) keys.clone(),
                (HashMap<Integer, Integer>) pressedKeys.clone());
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
     * Static method to clear all single-frame mapped keys. This function shall be called once at the end of the render
     * frame (before native event polling).
     */
    public static void clear() {
        pressedKeys.clear();
    }

    //endregion

    //region internal classes

    public static class KeyboardInputHandler extends GLFWKeyCallback {

        @Override
        public void invoke(long window, int key, int scanCode, int action, int mods) {
            keys.put(key, action);
            if (action == GLFW_PRESS) {
                pressedKeys.put(key, action);
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

    //endregion
}
