/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.processor.property;

import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.StylePropertyValue;
import org.pixel.gui.style.properties.OriginStyle;
import org.pixel.gui.style.model.DisplayUnit;

import java.util.List;

public class OriginPropertyProcessor implements StylePropertyProcessor {

    /**
     * Returns true if the style processor can process the style
     *
     * @param selectorName
     * @return
     */
    @Override
    public boolean match(String selectorName) {
        return selectorName.equalsIgnoreCase("origin");
    }

    /**
     * @param declaration
     * @param values
     * @return
     */
    @Override
    public StyleProperty process(String declaration, List<StylePropertyValue> values) {
        if (values.size() == 2) {
            return OriginStyle.builder()
                    .x(DisplayUnit.fromString(values.get(0).getValue()))
                    .y(DisplayUnit.fromString(values.get(1).getValue()))
                    .build();

        } else if (values.size() == 1) {
            return OriginStyle.builder()
                    .x(DisplayUnit.fromString(values.get(0).getValue()))
                    .y(DisplayUnit.fromString(values.get(0).getValue()))
                    .build();
        }

        return null;
    }
}
