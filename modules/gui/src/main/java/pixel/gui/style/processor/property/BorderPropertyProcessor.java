/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.processor.property;

import pixel.graphics.Color;
import pixel.gui.style.StyleProperty;
import pixel.gui.style.StylePropertyValue;
import pixel.gui.style.StyleUtils;
import pixel.gui.style.properties.BorderStyle;

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
