/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.properties;

import lombok.*;
import pixel.graphics.Color;
import pixel.gui.style.StyleProperty;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ColorStyle extends StyleProperty {

    private static final String NAME = "colorStyle";

    private Color color;

    /**
     * Merge property into existing values (when applicable)
     *
     * @param other
     */
    @Override
    public void merge(StyleProperty other) {
        if (other instanceof ColorStyle) {
            var o = (ColorStyle) other;
            this.color = o.color != null ? o.color : this.color;
        }
    }

    /**
     * Set missing property values
     *
     * @return
     */
    @Override
    public void setUnassignedProperties() {
        if (this.color == null) this.color = Color.WHITE;
    }

    @Override
    public StyleProperty clone() {
        return ColorStyle.builder()
                .color(new Color(color))
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
