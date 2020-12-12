/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.processor.property;

import pixel.gui.style.StyleProperty;
import pixel.gui.style.StylePropertyValue;
import pixel.gui.style.model.CursorType;
import pixel.gui.style.properties.CursorStyle;

import java.util.List;

public class CursorPropertyProcessor implements StylePropertyProcessor {

    /**
     * Returns true if the style processor can process the style
     *
     * @param selectorName
     * @return
     */
    @Override
    public boolean match(String selectorName) {
        return selectorName.equalsIgnoreCase("cursor");
    }

    /**
     * @param declaration
     * @param values
     * @return
     */
    @Override
    public StyleProperty process(String declaration, List<StylePropertyValue> values) {
        return CursorStyle.builder()
                .type(CursorType.fromString(values.get(0).getValue()))
                .build();
    }
}
