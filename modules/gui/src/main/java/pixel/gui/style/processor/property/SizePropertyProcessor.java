/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.processor.property;

import pixel.gui.style.StyleProperty;
import pixel.gui.style.StylePropertyValue;
import pixel.gui.style.model.DisplayUnit;
import pixel.gui.style.properties.SizeStyle;

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
