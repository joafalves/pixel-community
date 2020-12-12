/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.properties;

import lombok.*;
import pixel.graphics.Color;
import pixel.gui.model.BoxGradient;
import pixel.gui.style.StyleProperty;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class BoxGradientBackgroundStyle extends BackgroundStyle {

    private BoxGradient gradient;

    /**
     * Merge property into existing values (when applicable)
     *
     * @param other
     */
    @Override
    public void merge(StyleProperty other) {
        if (other instanceof BoxGradientBackgroundStyle) {
            var o = (BoxGradientBackgroundStyle) other;
            if (o.gradient.getStartColor() != null) gradient.setStartColor(o.gradient.getStartColor());
            if (o.gradient.getEndColor() != null) gradient.setEndColor(o.gradient.getEndColor());
            if (o.gradient.getEndColor() != null) gradient.setEndColor(o.gradient.getEndColor());
            if (o.gradient.getFeather() != null) gradient.setFeather(o.gradient.getFeather());
            if (o.gradient.getRadius() != null) gradient.setRadius(o.gradient.getRadius());
        }
    }

    /**
     * Set missing property values
     *
     * @return
     */
    @Override
    public void setUnassignedProperties() {
        if (this.gradient == null)
            gradient = new BoxGradient(Color.TRANSPARENT, Color.TRANSPARENT, 0.f, 0.5f);
    }

    @Override
    public StyleProperty clone() {
        return BoxGradientBackgroundStyle.builder()
                .gradient(new BoxGradient(this.gradient))
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
