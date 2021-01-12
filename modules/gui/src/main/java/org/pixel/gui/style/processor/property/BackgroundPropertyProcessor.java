/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.processor.property;

import org.pixel.graphics.Color;
import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.StylePropertyValue;
import org.pixel.gui.style.StylePropertyValueFunction;
import org.pixel.gui.style.properties.SolidBackgroundStyle;
import org.pixel.gui.style.processor.function.StyleFunctionProcessor;
import org.pixel.gui.style.processor.function.StyleFunctionProcessorFactory;

import java.util.List;

public class BackgroundPropertyProcessor implements StylePropertyProcessor {

    /**
     * Returns true if the style processor can process the style
     *
     * @param selectorName
     * @return
     */
    @Override
    public boolean match(String selectorName) {
        return selectorName.equalsIgnoreCase("background") || selectorName.equalsIgnoreCase("background-color");
    }

    /**
     * @param declaration
     * @param values
     * @return
     */
    @Override
    public StyleProperty process(String declaration, List<StylePropertyValue> values) {
        if (declaration.equalsIgnoreCase("background-color")) {
            return processBackgroundColor(values);

        } else if (declaration.equalsIgnoreCase("background")) {
            return processBackground(values);
        }

        return null;
    }

    private StyleProperty processBackground(List<StylePropertyValue> values) {
        // https://www.w3schools.com/cssref/css3_pr_background.asp
        // note for now.. we are considering only 1 format: bg-color
        // TODO: add support for org.pixel.other formats..
        return processBackgroundColor(values);
    }

    private StyleProperty processBackgroundColor(List<StylePropertyValue> values) {
        // https://www.w3schools.com/cssref/pr_background-color.asp
        if (values.size() != 1) {
            return null;
        }

        if (values.get(0) instanceof StylePropertyValueFunction) {
            StylePropertyValueFunction property = (StylePropertyValueFunction) values.get(0);
            StyleFunctionProcessor processor =
                    StyleFunctionProcessorFactory.getFunctionProcessor(property.getFunctionName());

            if (processor != null) {
                return processor.process(property);
            }

            return null;
        }

        return SolidBackgroundStyle.builder()
                .color(Color.fromString(values.get(0).getValue()))
                .build();
    }
}
