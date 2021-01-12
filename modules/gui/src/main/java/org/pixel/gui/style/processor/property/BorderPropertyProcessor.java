/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.processor.property;

import org.pixel.graphics.Color;
import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.StylePropertyValue;
import org.pixel.gui.style.StyleUtils;
import org.pixel.gui.style.properties.BorderStyle;

import java.util.List;

public class BorderPropertyProcessor implements StylePropertyProcessor {

    /**
     * Returns true if the style processor can process the style
     *
     * @param selectorName
     * @return
     */
    @Override
    public boolean match(String selectorName) {
        // TODO: add others
        return selectorName.equalsIgnoreCase("border") ||
                selectorName.equalsIgnoreCase("border-width") ||
                selectorName.equalsIgnoreCase("border-color") ||
                selectorName.equalsIgnoreCase("border-radius");
    }

    /**
     * @param declaration
     * @param values
     * @return
     */
    @Override
    public StyleProperty process(String declaration, List<StylePropertyValue> values) {
        if (declaration.equalsIgnoreCase("border-width")) {
            return BorderStyle.builder()
                    .width(StyleUtils.stripUnitValue((values.get(0).getValue())))
                    .build();

        } else if (declaration.equalsIgnoreCase("border-radius")) {
            return BorderStyle.builder()
                    .radius(StyleUtils.stripUnitValue((values.get(0).getValue())))
                    .build();

        } else if (declaration.equalsIgnoreCase("border-color")) {
            return BorderStyle.builder()
                    .color(Color.fromString(values.get(0).getValue()))
                    .build();
        }

        return null;
    }

}
