package org.pixel.ext.weaver.style.property.type;

public enum BoxSizingType {
    CONTENT_BOX,    // Width and height apply to content box only.
                    // Total size = content + padding + border.
    BORDER_BOX;     // Width and height include padding and border.
                    // Content size = width - padding - border.

    public static BoxSizingType defaultValue() {
        return CONTENT_BOX;
    }
}
