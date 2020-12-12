/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.commons.model;

public enum VerticalAlignment {
    BOTTOM, MIDDLE, TOP;

    /**
     * @param value
     * @return
     */
    public static VerticalAlignment fromString(String value) {
        switch (value.toLowerCase()) {
            case "top":
                return TOP;

            case "middle":
                return MIDDLE;

            case "bottom":
                return BOTTOM;

            default:
                return null;
        }
    }
}
