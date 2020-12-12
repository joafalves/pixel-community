/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.processor.function;

import pixel.gui.style.StyleProperty;
import pixel.gui.style.StylePropertyValueFunction;

public interface StyleFunctionProcessor {

    /**
     * @param functionName
     * @return
     */
    boolean match(String functionName);

    /**
     * Process property function
     *
     * @param function
     * @return
     */
    StyleProperty process(StylePropertyValueFunction function);

}
