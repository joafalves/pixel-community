/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.properties;

import lombok.*;
import org.pixel.graphics.Color;
import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.model.LinearGradient;
import org.pixel.math.Vector2;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class LinearGradientBackgroundStyle extends BackgroundStyle {

    private LinearGradient gradient;

    /**
     * Merge property into existing values (when applicable)
     *
     * @param other
     */
    @Override
    public void merge(StyleProperty other) {
        if (other instanceof LinearGradientBackgroundStyle) {
            var o = (LinearGradientBackgroundStyle) other;
            if (o.gradient.getStartColor() != null) gradient.setStartColor(o.gradient.getStartColor());
            if (o.gradient.getEndColor() != null) gradient.setEndColor(o.gradient.getEndColor());
            if (o.gradient.getEndColor() != null) gradient.setEndColor(o.gradient.getEndColor());
            if (o.gradient.getStartPosition() != null) gradient.setStartPosition(o.gradient.getStartPosition());
            if (o.gradient.getEndPosition() != null) gradient.setEndPosition(o.gradient.getEndPosition());
        }
    }

    /**
     * Set missing property values
     */
    @Override
    public void setUnassignedProperties() {
        if (this.gradient == null)
            gradient = new LinearGradient(Color.TRANSPARENT, Color.TRANSPARENT, Vector2.zero(), Vector2.one());
    }

    @Override
    public StyleProperty clone() {
        return LinearGradientBackgroundStyle.builder()
                .gradient(new LinearGradient(this.gradient))
                .build();
    }

    /**
     * @return
     */
    @Override
    public String getName() {
        return NAME;
    }
}
