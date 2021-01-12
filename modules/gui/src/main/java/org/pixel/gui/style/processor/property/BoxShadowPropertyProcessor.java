/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.processor.property;

import org.pixel.graphics.Color;
import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.StylePropertyValue;
import org.pixel.gui.style.StyleUtils;
import org.pixel.gui.style.properties.BoxShadowStyle;
import org.pixel.gui.style.model.ShadowType;

import java.util.List;

public class BoxShadowPropertyProcessor implements StylePropertyProcessor {

    /**
     * Returns true if the style processor can process the style
     *
     * @param selectorName
     * @return
     */
    @Override
    public boolean match(String selectorName) {
        return selectorName.equalsIgnoreCase("box-shadow");
    }

    /**
     * @param declaration
     * @param values
     * @return
     */
    @Override
    public StyleProperty process(String declaration, List<StylePropertyValue> values) {
        if (values.size() == 1) {
            return BoxShadowStyle.builder()
                    .color(Color.fromString(values.get(0).getValue()))
                    .build();

        } else if (values.size() == 3) {
            return BoxShadowStyle.builder()
                    .horizontalOffset(StyleUtils.stripUnitValue(values.get(0).getValue()))
                    .verticalOffset(StyleUtils.stripUnitValue(values.get(1).getValue()))
                    .color(Color.fromString(values.get(2).getValue()))
                    .build();

        } else if (values.size() == 4) {
            return BoxShadowStyle.builder()
                    .horizontalOffset(StyleUtils.stripUnitValue(values.get(0).getValue()))
                    .verticalOffset(StyleUtils.stripUnitValue(values.get(1).getValue()))
                    .blur(StyleUtils.stripUnitValue(values.get(2).getValue()))
                    .color(Color.fromString(values.get(3).getValue()))
                    .build();

        } else if (values.size() == 5) {
            return BoxShadowStyle.builder()
                    .horizontalOffset(StyleUtils.stripUnitValue(values.get(0).getValue()))
                    .verticalOffset(StyleUtils.stripUnitValue(values.get(1).getValue()))
                    .blur(StyleUtils.stripUnitValue(values.get(2).getValue()))
                    .spread(StyleUtils.stripUnitValue(values.get(3).getValue()))
                    .color(Color.fromString(values.get(4).getValue()))
                    .build();

        } else if (values.size() == 6) {
            return BoxShadowStyle.builder()
                    .horizontalOffset(StyleUtils.stripUnitValue(values.get(0).getValue()))
                    .verticalOffset(StyleUtils.stripUnitValue(values.get(1).getValue()))
                    .blur(StyleUtils.stripUnitValue(values.get(2).getValue()))
                    .spread(StyleUtils.stripUnitValue(values.get(3).getValue()))
                    .color(Color.fromString(values.get(4).getValue()))
                    .shadowType(ShadowType.fromString(values.get(5).getValue()))
                    .build();
        }

        return null;
    }
}
