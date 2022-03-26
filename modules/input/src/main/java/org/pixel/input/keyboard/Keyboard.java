/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

public class Keyboard {

    //region singleton

    static {
        keys = new HashMap<>();
        charListeners = new ArrayList<>();
    }

    //endregion

    //region properties

    private static final HashMap<Integer, Integer> keys;
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
        Integer value = keys.get(key.getValue());
        return value != null && value > 0;
    }

    /**
     * Static method to determine if a key is up.
     *
     * @param key The key to check.
     * @return True if the key is up.
     */
    public static boolean isKeyUp(KeyboardKey key) {
        Integer value = keys.get(key.getValue());
        return value != null && value == 0;
    }

    /**
     * Static method to get the current keyboard state.
     *
     * @return The current keyboard state.
     */
    public static KeyboardState getState() {
        return new KeyboardState((HashMap<Integer, Integer>) keys.clone());
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

    //endregion

    //region internal classes

    public static class KeyboardInputHandler extends GLFWKeyCallback {

        @Override
        public void invoke(long window, int key, int scanCode, int action, int mods) {
            keys.put(key, action);
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
