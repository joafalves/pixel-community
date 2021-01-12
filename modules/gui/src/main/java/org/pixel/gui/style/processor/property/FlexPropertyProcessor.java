/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.processor.property;

import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.StylePropertyValue;
import org.pixel.gui.style.properties.FlexStyle;
import org.pixel.gui.style.model.DisplayUnit;
import org.pixel.gui.style.model.FlexDirection;
import org.pixel.gui.style.model.FlexWrap;

import java.util.List;

public class FlexPropertyProcessor implements StylePropertyProcessor {

    /**
     * Returns true if the style processor can process the style
     *
     * @param selectorName
     * @return
     */
    @Override
    public boolean match(String selectorName) {
        return selectorName.equalsIgnoreCase("flex") ||
                selectorName.equalsIgnoreCase("flex-basis") ||
                selectorName.equalsIgnoreCase("flex-grow") ||
                selectorName.equalsIgnoreCase("flex-shrink") ||
                selectorName.equalsIgnoreCase("flex-wrap") ||
                selectorName.equalsIgnoreCase("flex-direction");
    }

    /**
     * @param declaration
     * @param values
     * @return
     */
    @Override
    public StyleProperty process(String declaration, List<StylePropertyValue> values) {
        switch (declaration.toLowerCase()) {
            case "flex":
                return processFlex(values);
            case "flex-basis":
                return processFlexBasis(values);
            case "flex-grow":
                return processFlexGrow(values);
            case "flex-shrink":
                return processFlexShrink(values);
            case "flex-wrap":
                return processFlexWrap(values);
            case "flex-direction":
                return processFlexDirection(values);
            default:
                return null;
        }
    }

    private StyleProperty processFlex(List<StylePropertyValue> values) {
        if (values.size() == 3) {
            return FlexStyle.builder()
                    .grow(DisplayUnit.fromString(values.get(0).getValue()))
                    .shrink(DisplayUnit.fromString(values.get(1).getValue()))
                    .basis(DisplayUnit.fromString(values.get(2).getValue()))
                    .build();

        } else if (values.size() == 1) {
            return FlexStyle.builder()
                    .grow(DisplayUnit.fromString(values.get(0).getValue()))
                    .shrink(DisplayUnit.fromString(values.get(0).getValue()))
                    .basis(DisplayUnit.fromString(values.get(0).getValue()))
                    .build();
        }

        return null;
    }

    private StyleProperty processFlexBasis(List<StylePropertyValue> values) {
        if (values.size() == 1) {
            return FlexStyle.builder()
                    .basis(DisplayUnit.fromString(values.get(0).getValue()))
                    .build();
        }

        return null;
    }

    private StyleProperty processFlexGrow(List<StylePropertyValue> values) {
        if (values.size() == 1) {
            return FlexStyle.builder()
                    .grow(DisplayUnit.fromString(values.get(0).getValue()))
                    .build();
        }

        return null;
    }

    private StyleProperty processFlexShrink(List<StylePropertyValue> values) {
        if (values.size() == 1) {
            return FlexStyle.builder()
                    .shrink(DisplayUnit.fromString(values.get(0).getValue()))
                    .build();
        }

        return null;
    }

    private StyleProperty processFlexWrap(List<StylePropertyValue> values) {
        if (values.size() == 1) {
            return FlexStyle.builder()
                    .wrap(FlexWrap.fromString(values.get(0).getValue()))
                    .build();
        }

        return null;
    }

    private StyleProperty processFlexDirection(List<StylePropertyValue> values) {
        if (values.size() == 1) {
            return FlexStyle.builder()
                    .direction(FlexDirection.fromString(values.get(0).getValue()))
                    .build();
        }

        return null;
    }
}
