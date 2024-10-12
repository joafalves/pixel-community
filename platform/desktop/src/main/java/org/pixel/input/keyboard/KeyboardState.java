/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.keyboard;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_REPEAT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KeyboardState implements Serializable {

    //region properties

    private final HashMap<Integer, Integer> keys;
    private final HashMap<Integer, Integer> pressedKeys;

    //endregion

    //region constructors

    /**
     * Constructor.
     *
     * @param keys The snapshot of the keyboard state.
     * @param pressedKeys The snapshot of the pressed keys.
     */
    protected KeyboardState(HashMap<Integer, Integer> keys, HashMap<Integer, Integer> pressedKeys) {
        this.keys = keys;
        this.pressedKeys = pressedKeys;
    }

    //endregion

    //region public methods

    /**
     * Indicates whether the specified key is currently down.
     *
     * @param key The key to check.
     * @return True if the key is down, false otherwise.
     */
    public boolean isKeyDown(KeyboardKey key) {
        return this.isKeyDown(key.getValue());
    }

    /**
     * Indicates whether the specified key is currently down.
     *
     * @param key The key to check.
     * @return True if the key is down, false otherwise.
     */
    public boolean isKeyDown(int key) {
        var value = this.keys.get(key);
        return value != null && (value == GLFW_PRESS || value == GLFW_REPEAT);
    }

    /**
     * Indicates whether the specified key is currently up.
     *
     * @param key The key to check.
     * @return True if the key is up, false otherwise.
     */
    public boolean isKeyUp(KeyboardKey key) {
        return this.isKeyUp(key.getValue());
    }

    /**
     * Indicates whether the specified key is currently up.
     *
     * @param key The key to check.
     * @return True if the key is up, false otherwise.
     */
    public boolean isKeyUp(int key) {
        var value = this.keys.get(key);
        return value == null || value == GLFW_RELEASE;
    }

    /**
     * Indicates whether the specified key is currently pressed.
     *
     * @param key The key to check.
     * @return True if the key is pressed, false otherwise.
     */
    public boolean isKeyPressed(KeyboardKey key) {
        return this.isKeyPressed(key.getValue());
    }

    /**
     * Indicates whether the specified key is currently pressed.
     *
     * @param key The key to check.
     * @return True if the key is pressed, false otherwise.
     */
    public boolean isKeyPressed(int key) {
        var value = this.pressedKeys.get(key);
        return value != null && value == GLFW_PRESS;
    }

    /**
     * Returns an array of keys that are on a down state.
     *
     * @return An array of keys that are on a down state
     */
    @Deprecated
    public List<Integer> downKeys() {
        List<Integer> downKeys = new ArrayList<>();
        this.keys.forEach((key, state) -> {
            if (state != null && state > 0) {
                downKeys.add(key);
            }
        });
        return downKeys;
    }

    /**
     * Get the keys that are on the current snapshot.
     *
     * @return The keys that are on the current snapshot.
     */
    public HashMap<Integer, Integer> getKeys() {
        return keys;
    }

    //endregion
}
