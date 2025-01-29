/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.attribute;

public enum VerticalAlignment {
    BOTTOM, MIDDLE, TOP;

    /**
     * Returns the vertical alignment based on the given string.
     *
     * @param value The string value.
     * @return The vertical alignment instance.
     */
    public static VerticalAlignment fromString(String value) {
        return switch (value.toLowerCase()) {
            case "top" -> TOP;
            case "middle" -> MIDDLE;
            case "bottom" -> BOTTOM;
            default -> null;
        };
    }
}
