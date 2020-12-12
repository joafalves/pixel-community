/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.model;

public enum FlexWrap {
    NO_WRAP, WRAP, WRAP_REVERSE;

    /**
     * @param value
     * @return
     */
    public static FlexWrap fromString(String value) {
        switch (value.toLowerCase()) {
            case "nowrap":
                return NO_WRAP;

            case "wrap":
                return WRAP;

            case "wrap-reverse":
                return WRAP_REVERSE;

            default:
                return getDefault();
        }
    }

    public static FlexWrap getDefault() {
        return NO_WRAP;
    }
}
