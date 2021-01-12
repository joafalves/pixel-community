/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.commons.model;

public enum HorizontalAlignment {
    LEFT, CENTER, RIGHT;

    /**
     * @param value
     * @return
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
