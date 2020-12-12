/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.properties;

import lombok.*;
import pixel.graphics.Color;
import pixel.gui.style.StyleProperty;
import pixel.gui.style.model.BorderType;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class BorderStyle extends StyleProperty {

    private static final String NAME = "borderStyle";

    private Float width;
    private BorderType borderType;
    private Color color;
    private Float radius;

    /**
     * Merge property into existing values (when applicable)
     *
     * @param other
     */
    @Override
    public void merge(StyleProperty other) {
        if (other instanceof BorderStyle) {
            var o = (BorderStyle) other;
            this.color = o.color != null ? o.color : this.color;
            this.width = o.width != null ? o.width : this.width;
            this.borderType = o.borderType != null ? o.borderType : this.borderType;
            this.radius = o.radius != null ? o.radius : this.radius;
        }
    }

    /**
     * Set missing property values
     *
     * @return
     */
    @Override
    public void setUnassignedProperties() {
        if (this.color == null) this.color = Color.TRANSPARENT;
        if (this.width == null) this.width = 0.f;
        if (this.borderType == null) this.borderType = BorderType.NONE;
        if (this.radius == null) this.radius = 0.f;
    }

    @Override
    public StyleProperty clone() {
        return BorderStyle.builder()
                .color(new Color(color))
                .width(width)
                .borderType(borderType)
                .radius(radius)
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
