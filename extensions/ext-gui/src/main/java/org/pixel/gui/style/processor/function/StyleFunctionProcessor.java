/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.processor.function;

import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.StylePropertyValueFunction;

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
