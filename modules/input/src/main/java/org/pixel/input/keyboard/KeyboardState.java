/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.input.keyboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KeyboardState implements Serializable {

    //region properties

    private final HashMap<Integer, Integer> keys;

    //endregion

    //region constructors

    /**
     * Constructor.
     *
     * @param keys The snapshot of the keyboard state.
     */
    public KeyboardState(HashMap<Integer, Integer> keys) {
        this.keys = keys;
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
        Integer value = this.keys.get(key);
        return value != null && value > 0;
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
        Integer value = this.keys.get(key);
        return value == null || value == 0;
    }

    /**
     * Returns an array of keys that are on a down state.
     *
     * @return An array of keys that are on a down state
     */
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
