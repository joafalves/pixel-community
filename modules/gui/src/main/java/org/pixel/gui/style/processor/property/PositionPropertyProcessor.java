/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.processor.property;

import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.StylePropertyValue;
import org.pixel.gui.style.properties.PositionStyle;
import org.pixel.gui.style.model.DisplayUnit;

import java.util.List;

public class PositionPropertyProcessor implements StylePropertyProcessor {

    /**
     * Returns true if the style processor can process the style
     *
     * @param selectorName
     * @return
     */
    @Override
    public boolean match(String selectorName) {
        return selectorName.equalsIgnoreCase("position") ||
                selectorName.equalsIgnoreCase("left") ||
                selectorName.equalsIgnoreCase("top") ||
                selectorName.equalsIgnoreCase("right") ||
                selectorName.equalsIgnoreCase("bottom");
    }

    /**
     * @param declaration
     * @param values
     * @return
     */
    @Override
    public StyleProperty process(String declaration, List<StylePropertyValue> values) {
        // https://www.w3schools.com/css/css_positioning.asp
        if (declaration.equalsIgnoreCase("top")) {
            if (values.size() == 1) {
                return PositionStyle.builder()
                        .top(DisplayUnit.fromString(values.get(0).getValue()))
                        .build();
            }

        } else if (declaration.equalsIgnoreCase("right")) {
            if (values.size() == 1) {
                return PositionStyle.builder()
                        .right(DisplayUnit.fromString(values.get(0).getValue()))
                        .build();
            }

        } else if (declaration.equalsIgnoreCase("bottom")) {
            if (values.size() == 1) {
                return PositionStyle.builder()
                        .bottom(DisplayUnit.fromString(values.get(0).getValue()))
                        .build();
            }

        } else if (declaration.equalsIgnoreCase("left")) {
            if (values.size() == 1) {
                return PositionStyle.builder()
                        .left(DisplayUnit.fromString(values.get(0).getValue()))
                        .build();
            }
        }

        return null;
    }
}

