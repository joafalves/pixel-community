package org.pixel.ext.weaver.style.property.type;

public enum PositionType {
    RELATIVE,
    ABSOLUTE,
    FIXED;

    public static PositionType defaultValue() {
        return RELATIVE;
    }
}
