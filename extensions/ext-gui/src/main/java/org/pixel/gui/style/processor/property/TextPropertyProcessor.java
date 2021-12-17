/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.processor.property;

import org.pixel.commons.attribute.HorizontalAlignment;
import org.pixel.commons.attribute.VerticalAlignment;
import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.StylePropertyValue;
import org.pixel.gui.style.properties.TextStyle;

import java.util.List;

public class TextPropertyProcessor implements StylePropertyProcessor {

    /**
     * Returns true if the style processor can process the style
     *
     * @param selectorName
     * @return
     */
    @Override
    public boolean match(String selectorName) {
        return selectorName.equalsIgnoreCase("text-align") ||
                selectorName.equalsIgnoreCase("text-h-align") ||
                selectorName.equalsIgnoreCase("text-v-align");
    }

    /**
     * @param declaration
     * @param values
     * @return
     */
    @Override
    public StyleProperty process(String declaration, List<StylePropertyValue> values) {
        if (declaration.equalsIgnoreCase("text-align")) {
            if (values.size() == 1) {
                return TextStyle.builder()
                        .horizontalAlignment(HorizontalAlignment.fromString(values.get(0).getValue()))
                        .build();

            } else if (values.size() == 2) {
                return TextStyle.builder()
                        .horizontalAlignment(HorizontalAlignment.fromString(values.get(0).getValue()))
                        .verticalAlignment(VerticalAlignment.fromString(values.get(1).getValue()))
                        .build();
            }

            return null;

        } else if (declaration.equalsIgnoreCase("text-h-align")) {
            return TextStyle.builder()
                    .horizontalAlignment(HorizontalAlignment.fromString(values.get(0).getValue()))
                    .build();

        } else if (declaration.equalsIgnoreCase("text-v-align")) {
            return TextStyle.builder()
                    .verticalAlignment(VerticalAlignment.fromString(values.get(0).getValue()))
                    .build();
        }

        return null;
    }
}
