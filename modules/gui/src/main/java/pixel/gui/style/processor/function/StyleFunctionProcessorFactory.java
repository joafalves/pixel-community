/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.processor.function;

import java.util.ArrayList;
import java.util.List;

public class StyleFunctionProcessorFactory {

    private static final List<StyleFunctionProcessor> processors;

    static {
        processors = new ArrayList<>();
        processors.add(new PixelGradientFunctionProcessor());
    }

    /**
     * Get appropriate function processor
     *
     * @param functionName
     * @return
     */
    public static StyleFunctionProcessor getFunctionProcessor(String functionName) {
        for (StyleFunctionProcessor processor : processors) {
            if (processor.match(functionName)) {
                return processor;
            }
        }

        return null;
    }
}
