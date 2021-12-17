/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.processor.property;

import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.StylePropertyValue;
import org.pixel.gui.style.properties.SizeStyle;
import org.pixel.gui.style.model.DisplayUnit;

import java.util.List;

public class SizePropertyProcessor implements StylePropertyProcessor {

    /**
     * Returns true if the style processor can process the style
     *
     * @param selectorName
     * @return
     */
    @Override
    public boolean match(String selectorName) {
        return selectorName.equalsIgnoreCase("width") ||
                selectorName.equalsIgnoreCase("height");
    }

    /**
     * @param declaration
     * @param values
     * @return
     */
    @Override
    public StyleProperty process(String declaration, List<StylePropertyValue> values) {
        if (declaration.equalsIgnoreCase("width")) {
            return SizeStyle.builder()
                    .width(DisplayUnit.fromString(values.get(0).getValue()))
                    .build();

        } else if (declaration.equalsIgnoreCase("height")) {
            return SizeStyle.builder()
                    .height(DisplayUnit.fromString(values.get(0).getValue()))
                    .build();
        }

        return null;
    }
}
