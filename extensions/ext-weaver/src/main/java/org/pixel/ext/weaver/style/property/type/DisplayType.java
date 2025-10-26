package org.pixel.ext.weaver.style.property.type;

public enum DisplayType {
    NONE,
    BLOCK,
    INLINE,
    FLEX,
    GRID;

    public static DisplayType defaultValue() {
        return BLOCK;
    }
}
