/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.model;

public enum DisplayType {
    BLOCK, FLEX, NONE;

    /**
     * @param value
     * @return
     */
    public static DisplayType fromString(String value) {
        switch (value.toLowerCase()) {
            case "block":
                return BLOCK;

            case "flex":
                return FLEX;

            case "hidden":
            case "none":
                return NONE;

            default:
                return getDefault();
        }
    }

    public static DisplayType getDefault() {
        return BLOCK;
    }
}
