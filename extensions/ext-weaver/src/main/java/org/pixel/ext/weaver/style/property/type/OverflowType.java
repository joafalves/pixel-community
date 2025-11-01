package org.pixel.ext.weaver.style.property.type;

public enum OverflowType {
    VISIBLE,
    HIDDEN,
    SCROLL,
    AUTO;

    public static OverflowType defaultValue() {
        return VISIBLE;
    }

    public boolean hasClipping() {
        return this == HIDDEN || this == SCROLL;
    }
}
