/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.keyboard;

import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.HashMap;

public class Keyboard {

    //region singleton

    static {
        keys = new HashMap<>();
    }

    //endregion

    //region properties

    private static final HashMap<Integer, Integer> keys;

    //endregion

    //region public methods

    /**
     * @param key
     * @return
     */
    public static boolean isKeyDown(KeyboardKey key) {
        Integer value = keys.get(key.getValue());
        return value != null && value > 0;
    }

    /**
     * @param key
     * @return
     */
    public static boolean isKeyUp(KeyboardKey key) {
        Integer value = keys.get(key.getValue());
        return value != null && value == 0;
    }

    public static KeyboardState getState() {
        return new KeyboardState((HashMap<Integer, Integer>) keys.clone());
    }

    //endregion

    //region internal classes

    /**
     * Keyboard Input Handler
     */
    public static class KeyboardInputHandler extends GLFWKeyCallback {

        @Override
        public void invoke(long window, int key, int scanCode, int action, int mods) {
            keys.put(key, action);
        }

    }

    //endregion
}
