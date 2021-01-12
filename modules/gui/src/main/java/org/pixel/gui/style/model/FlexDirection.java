/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.model;

public enum FlexDirection {
    ROW, ROW_REVERSE, COLUMN, COLUMN_REVERSE;

    /**
     * @param value
     * @return
     */
    public static FlexDirection fromString(String value) {
        switch (value.toLowerCase()) {
            case "row":
                return ROW;

            case "row-reverse":
                return ROW_REVERSE;

            case "column":
                return COLUMN;

            case "column-reverse":
                return COLUMN_REVERSE;

            default:
                return getDefault();
        }
    }

    public static FlexDirection getDefault() {
        return ROW;
    }
}
