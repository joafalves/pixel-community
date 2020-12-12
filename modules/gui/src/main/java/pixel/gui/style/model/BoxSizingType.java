/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.model;

public enum BoxSizingType {
    CONTENT_BOX, BORDER_BOX;

    /**
     * @param value
     * @return
     */
    public static BoxSizingType fromString(String value) {
        switch (value.toLowerCase()) {
            case "content-box":
                return CONTENT_BOX;
            case "border-box":
                return BORDER_BOX;
            default:
                return null;
        }
    }

    /**
     * @return
     */
    public static BoxSizingType getDefault() {
        return BORDER_BOX;
    }
}
