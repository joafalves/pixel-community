/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.processor.function;

import org.pixel.graphics.Color;
import org.pixel.gui.style.StyleFunctions;
import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.StylePropertyValueFunction;
import org.pixel.gui.style.properties.BoxGradientBackgroundStyle;
import org.pixel.gui.style.properties.LinearGradientBackgroundStyle;
import org.pixel.math.Vector2;
import org.pixel.gui.model.BoxGradient;
import org.pixel.gui.model.LinearGradient;

import java.util.List;

public class PixelGradientFunctionProcessor implements StyleFunctionProcessor {

    /**
     * @param functionName
     * @return
     */
    @Override
    public boolean match(String functionName) {
        return functionName.equalsIgnoreCase(StyleFunctions.PX_LINEAR_GRADIENT) ||
                functionName.equalsIgnoreCase(StyleFunctions.PX_BOX_GRADIENT);
    }

    /**
     * Process property function
     *
     * @param function
     * @return
     */
    @Override
    public StyleProperty process(StylePropertyValueFunction function) {
        if (function.getFunctionName().equalsIgnoreCase(StyleFunctions.PX_LINEAR_GRADIENT)) {
            return processLinearGradient(function);

        } else if (function.getFunctionName().equalsIgnoreCase(StyleFunctions.PX_BOX_GRADIENT)) {
            return processBoxGradient(function);
        }

        return null;
    }

    /**
     * @param function
     * @return
     */
    private StyleProperty processLinearGradient(StylePropertyValueFunction function) {
        List<String> params = function.getParameters();
        if (params.size() == 2) {
            // fun(#color1, #color2)
            var gradient = LinearGradient.builder()
                    .startPosition(new Vector2(.5f, 0.f))
                    .endPosition(new Vector2(.5f, 1.f))
                    .startColor(Color.fromString(params.get(0)))
                    .endColor(Color.fromString(params.get(1)))
                    .build();

            return LinearGradientBackgroundStyle.builder()
                    .gradient(gradient)
                    .build();

        } else if (params.size() == 6) {
            // fun(sx, sy, ex, ey, #color1, #color2)
            var gradient = LinearGradient.builder()
                    .startPosition(new Vector2(Float.parseFloat(params.get(0)), Float.parseFloat(params.get(1))))
                    .endPosition(new Vector2(Float.parseFloat(params.get(2)), Float.parseFloat(params.get(3))))
                    .startColor(Color.fromString(params.get(4)))
                    .endColor(Color.fromString(params.get(5)))
                    .build();

            return LinearGradientBackgroundStyle.builder()
                    .gradient(gradient)
                    .build();
        }

        return null;
    }

    /**
     * @param function
     * @return
     */
    private StyleProperty processBoxGradient(StylePropertyValueFunction function) {
        List<String> params = function.getParameters();
        if (params.size() == 3) {
            // fun(#color1, #color2, feather)
            var gradient = BoxGradient.builder()
                    .startColor(Color.fromString(params.get(0)))
                    .endColor(Color.fromString(params.get(1)))
                    .feather(Float.parseFloat(params.get(2)))
                    .radius(0.5f)
                    .build();

            return BoxGradientBackgroundStyle.builder()
                    .gradient(gradient)
                    .build();

        } else if (params.size() == 4) {
            // fun(#color1, #color2, feather, radius)
            var gradient = BoxGradient.builder()
                    .startColor(Color.fromString(params.get(0)))
                    .endColor(Color.fromString(params.get(1)))
                    .feather(Float.parseFloat(params.get(2)))
                    .radius(Float.parseFloat(params.get(3)))
                    .build();

            return BoxGradientBackgroundStyle.builder()
                    .gradient(gradient)
                    .build();
        }

        return null;
    }
}
