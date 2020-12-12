/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.input.keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KeyboardState {

    //region properties

    private HashMap<Integer, Integer> keys;

    //endregion

    //region constructors

    /**
     * Constructor
     *
     * @param keys
     */
    public KeyboardState(HashMap<Integer, Integer> keys) {
        this.keys = keys;
    }

    //endregion

    //region public methods

    /**
     * @param key
     * @return
     */
    public boolean isKeyDown(KeyboardKey key) {
        return this.isKeyDown(key.getValue());
    }

    /**
     * @param key
     * @return
     */
    public boolean isKeyDown(int key) {
        Integer value = this.keys.get(key);
        return value != null && value > 0;
    }

    /**
     * @param key
     * @return
     */
    public boolean isKeyUp(KeyboardKey key) {
        return this.isKeyUp(key.getValue());
    }

    /**
     * @param key
     * @return
     */
    public boolean isKeyUp(int key) {
        Integer value = this.keys.get(key);
        return value == null || value == 0;
    }

    /**
     * Returns an array of keys that are on a down state
     *
     * @return
     */
    public List<Integer> downKeys() {
        List<Integer> downKeys = new ArrayList<>();
        this.keys.forEach((key, state) -> {
            if (state != null && state > 0) downKeys.add(key);
        });
        return downKeys;
    }

    /**
     * Get keys
     *
     * @return
     */
    public HashMap<Integer, Integer> getKeys() {
        return keys;
    }

    //endregion
}
