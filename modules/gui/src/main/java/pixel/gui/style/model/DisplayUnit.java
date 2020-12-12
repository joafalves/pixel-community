/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class DisplayUnit {

    private Float value;
    private Type type;

    public static DisplayUnit fromString(String value) {
        Type type;
        if (value.trim().endsWith("%")) {
            type = Type.PERCENTAGE;

        } else if (value.trim().equalsIgnoreCase("auto")) {
            return DisplayUnit.builder()
                    .value(0f)
                    .type(Type.AUTO)
                    .build();

        } else if (value.trim().endsWith("px")) {
            type = Type.PIXELS;

        } else {
            // not attached to any type
            type = Type.VALUE;
        }

        return DisplayUnit.builder()
                .value(Float.parseFloat(value.trim().replaceAll("px|%|em", "")))
                .type(type)
                .build();
    }

    @Override
    public DisplayUnit clone() {
        return DisplayUnit.builder()
                .value(value)
                .type(type)
                .build();
    }

    public enum Type {
        PIXELS, PERCENTAGE, AUTO, VALUE
    }
}
