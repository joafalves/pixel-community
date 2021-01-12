/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.processor.property;

import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.StylePropertyValue;
import org.pixel.gui.style.StyleUtils;
import org.pixel.gui.style.properties.PaddingStyle;

import java.util.List;

public class PaddingPropertyProcessor implements StylePropertyProcessor {

    /**
     * Returns true if the style processor can process the style
     *
     * @param selectorName
     * @return
     */
    @Override
    public boolean match(String selectorName) {
        return selectorName.equalsIgnoreCase("padding") ||
                selectorName.equalsIgnoreCase("padding-top") ||
                selectorName.equalsIgnoreCase("padding-bottom") ||
                selectorName.equalsIgnoreCase("padding-right") ||
                selectorName.equalsIgnoreCase("padding-left");
    }

    /**
     * @param declaration
     * @param values
     * @return
     */
    @Override
    public StyleProperty process(String declaration, List<StylePropertyValue> values) {
        // https://www.w3schools.com/css/css_padding.asp
        if (declaration.equalsIgnoreCase("padding")) {
            if (values.size() == 4) {
                // padding: <top> <right> <bottom> <left>
                return PaddingStyle.builder()
                        .top(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .right(StyleUtils.stripUnitValue((values.get(1).getValue())))
                        .bottom(StyleUtils.stripUnitValue((values.get(2).getValue())))
                        .left(StyleUtils.stripUnitValue((values.get(3).getValue())))
                        .build();

            } else if (values.size() == 3) {
                // padding: <top> <right,left> <bottom>
                return PaddingStyle.builder()
                        .top(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .right(StyleUtils.stripUnitValue((values.get(1).getValue())))
                        .left(StyleUtils.stripUnitValue((values.get(1).getValue())))
                        .bottom(StyleUtils.stripUnitValue((values.get(2).getValue())))
                        .build();

            } else if (values.size() == 2) {
                // padding: <top,bottom> <right,left>
                return PaddingStyle.builder()
                        .top(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .bottom(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .right(StyleUtils.stripUnitValue((values.get(1).getValue())))
                        .left(StyleUtils.stripUnitValue((values.get(1).getValue())))
                        .build();

            } else if (values.size() == 1) {
                // padding: <top,right,bottom,left>
                return PaddingStyle.builder()
                        .top(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .bottom(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .right(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .left(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .build();
            }

        } else if (declaration.equalsIgnoreCase("padding-top")) {
            if (values.size() == 1) {
                return PaddingStyle.builder()
                        .top(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .build();
            }

        } else if (declaration.equalsIgnoreCase("padding-right")) {
            if (values.size() == 1) {
                return PaddingStyle.builder()
                        .right(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .build();
            }

        } else if (declaration.equalsIgnoreCase("padding-bottom")) {
            if (values.size() == 1) {
                return PaddingStyle.builder()
                        .bottom(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .build();
            }

        } else if (declaration.equalsIgnoreCase("padding-left")) {
            if (values.size() == 1) {
                return PaddingStyle.builder()
                        .left(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .build();
            }
        }

        return null;
    }
}

