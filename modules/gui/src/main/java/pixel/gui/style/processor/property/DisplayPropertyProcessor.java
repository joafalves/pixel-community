/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.processor.property;

import pixel.gui.style.StyleProperty;
import pixel.gui.style.StylePropertyValue;
import pixel.gui.style.model.DisplayType;
import pixel.gui.style.properties.DisplayStyle;

import java.util.List;

public class DisplayPropertyProcessor implements StylePropertyProcessor {

    /**
     * Returns true if the style processor can process the style
     *
     * @param selectorName
     * @return
     */
    @Override
    public boolean match(String selectorName) {
        return selectorName.equalsIgnoreCase("display");
    }

    /**
     * @param declaration
     * @param values
     * @return
     */
    @Override
    public StyleProperty process(String declaration, List<StylePropertyValue> values) {
        if (values.size() == 1) {
            DisplayType type = DisplayType.fromString(values.get(0).getValue().trim());
            if (type != null) {
                return DisplayStyle.builder()
                        .type(type)
                        .build();
            }
        }

        return null;
    }
}
