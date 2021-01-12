/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.processor.property;

import org.pixel.graphics.Color;
import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.StylePropertyValue;
import org.pixel.gui.style.properties.ColorStyle;

import java.util.List;

public class ColorPropertyProcessor implements StylePropertyProcessor {

    /**
     * Returns true if the style processor can process the style
     *
     * @param selectorName
     * @return
     */
    @Override
    public boolean match(String selectorName) {
        return selectorName.equalsIgnoreCase("color");
    }

    /**
     * @param declaration
     * @param values
     * @return
     */
    @Override
    public StyleProperty process(String declaration, List<StylePropertyValue> values) {
        return ColorStyle.builder()
                .color(Color.fromString(values.get(0).getValue()))
                .build();
    }
}
