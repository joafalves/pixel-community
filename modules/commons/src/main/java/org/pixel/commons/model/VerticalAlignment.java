/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.model;

public enum VerticalAlignment {
    BOTTOM, MIDDLE, TOP;

    /**
     * Returns the vertical alignment based on the given string.
     *
     * @param value The string value.
     * @return The vertical alignment instance.
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
