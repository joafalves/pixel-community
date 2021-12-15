/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.model;

public enum HorizontalAlignment {
    LEFT, CENTER, RIGHT;

    /**
     * Returns the horizontal alignment based on the given string.
     *
     * @param value The string value.
     * @return The horizontal alignment instance.
     */
    public static HorizontalAlignment fromString(String value) {
        switch (value.toLowerCase()) {
            case "left":
                return LEFT;

            case "center":
                return CENTER;

            case "right":
                return RIGHT;

            default:
                return null;
        }
    }
}
