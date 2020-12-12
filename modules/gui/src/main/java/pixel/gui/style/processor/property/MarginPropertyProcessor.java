/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.processor.property;

import pixel.gui.style.StyleProperty;
import pixel.gui.style.StylePropertyValue;
import pixel.gui.style.StyleUtils;
import pixel.gui.style.properties.MarginStyle;

import java.util.List;

public class MarginPropertyProcessor implements StylePropertyProcessor {

    /**
     * Returns true if the style processor can process the style
     *
     * @param selectorName
     * @return
     */
    @Override
    public boolean match(String selectorName) {
        return selectorName.equalsIgnoreCase("margin") ||
                selectorName.equalsIgnoreCase("margin-top") ||
                selectorName.equalsIgnoreCase("margin-bottom") ||
                selectorName.equalsIgnoreCase("margin-right") ||
                selectorName.equalsIgnoreCase("margin-left");
    }

    /**
     * @param declaration
     * @param values
     * @return
     */
    @Override
    public StyleProperty process(String declaration, List<StylePropertyValue> values) {
        // https://www.w3schools.com/css/css_margin.asp
        if (declaration.equalsIgnoreCase("margin")) {
            if (values.size() == 4) {
                // margin: <top> <right> <bottom> <left>
                return MarginStyle.builder()
                        .top(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .right(StyleUtils.stripUnitValue((values.get(1).getValue())))
                        .bottom(StyleUtils.stripUnitValue((values.get(2).getValue())))
                        .left(StyleUtils.stripUnitValue((values.get(3).getValue())))
                        .build();

            } else if (values.size() == 3) {
                // margin: <top> <right,left> <bottom>
                return MarginStyle.builder()
                        .top(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .right(StyleUtils.stripUnitValue((values.get(1).getValue())))
                        .left(StyleUtils.stripUnitValue((values.get(1).getValue())))
                        .bottom(StyleUtils.stripUnitValue((values.get(2).getValue())))
                        .build();

            } else if (values.size() == 2) {
                // margin: <top,bottom> <right,left>
                return MarginStyle.builder()
                        .top(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .bottom(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .right(StyleUtils.stripUnitValue((values.get(1).getValue())))
                        .left(StyleUtils.stripUnitValue((values.get(1).getValue())))
                        .build();

            } else if (values.size() == 1) {
                // margin: <top,right,bottom,left>
                return MarginStyle.builder()
                        .top(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .bottom(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .right(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .left(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .build();
            }

        } else if (declaration.equalsIgnoreCase("margin-top")) {
            if (values.size() == 1) {
                return MarginStyle.builder()
                        .top(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .build();
            }

        } else if (declaration.equalsIgnoreCase("margin-right")) {
            if (values.size() == 1) {
                return MarginStyle.builder()
                        .right(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .build();
            }

        } else if (declaration.equalsIgnoreCase("margin-bottom")) {
            if (values.size() == 1) {
                return MarginStyle.builder()
                        .bottom(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .build();
            }

        } else if (declaration.equalsIgnoreCase("margin-left")) {
            if (values.size() == 1) {
                return MarginStyle.builder()
                        .left(StyleUtils.stripUnitValue((values.get(0).getValue())))
                        .build();
            }
        }

        return null;
    }
}

