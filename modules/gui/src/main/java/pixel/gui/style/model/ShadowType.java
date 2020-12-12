/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.model;

public enum ShadowType {
    OUTER, INNER;

    public static ShadowType fromString(String value) {
        switch (value.toLowerCase()) {
            case "outer":
                return OUTER;

            case "inner":
                return INNER;

            default:
                return getDefault();
        }
    }

    public static ShadowType getDefault() {
        return OUTER;
    }
}
