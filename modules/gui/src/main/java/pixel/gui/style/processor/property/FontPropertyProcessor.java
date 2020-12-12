/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.processor.property;

import pixel.gui.style.StyleProperty;
import pixel.gui.style.StylePropertyValue;
import pixel.gui.style.StyleUtils;
import pixel.gui.style.properties.FontStyle;

import java.util.List;

public class FontPropertyProcessor implements StylePropertyProcessor {

    /**
     * Returns true if the style processor can process the style
     *
     * @param selectorName
     * @return
     */
    @Override
    public boolean match(String selectorName) {
        return selectorName.equalsIgnoreCase("font-family") ||
                selectorName.equalsIgnoreCase("font-size");
    }

    /**
     * @param declaration
     * @param values
     * @return
     */
    @Override
    public StyleProperty process(String declaration, List<StylePropertyValue> values) {
        if (declaration.equalsIgnoreCase("font-size")) {
            return FontStyle.builder()
                    .fontSize(StyleUtils.stripUnitValue((values.get(0).getValue())))
                    .build();

        } else if (declaration.equalsIgnoreCase("font-family")) {
            return FontStyle.builder()
                    .fontFamily(values.get(0).getValue().replaceAll("\"", "").replaceAll("\'", ""))
                    .build();
        }

        return null;
    }
}
