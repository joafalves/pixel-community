/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.processor.property;

import pixel.gui.style.StyleProperty;
import pixel.gui.style.StylePropertyValue;

import java.util.List;

public interface StylePropertyProcessor {

    /**
     * Returns true if the style processor can process the style
     *
     * @param selectorName
     * @return
     */
    boolean match(String selectorName);

    /**
     * @param declaration
     * @param values
     * @return
     */
    StyleProperty process(String declaration, List<StylePropertyValue> values);

}
