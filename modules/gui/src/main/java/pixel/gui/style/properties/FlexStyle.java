/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.properties;

import lombok.*;
import pixel.gui.style.StyleProperty;
import pixel.gui.style.model.DisplayUnit;
import pixel.gui.style.model.FlexDirection;
import pixel.gui.style.model.FlexWrap;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class FlexStyle extends StyleProperty {

    private static final String NAME = "flexStyle";

    private FlexDirection direction;
    private FlexWrap wrap;
    private DisplayUnit basis;
    private DisplayUnit grow;
    private DisplayUnit shrink;

    /**
     * Merge property into existing values (when applicable)
     *
     * @param other
     */
    @Override
    public void merge(StyleProperty other) {
        if (other instanceof FlexStyle) {
            var o = (FlexStyle) other;
            this.direction = o.direction != null ? o.direction : this.direction;
            this.wrap = o.wrap != null ? o.wrap : this.wrap;
            this.basis = o.basis != null ? o.basis : this.basis;
            this.grow = o.grow != null ? o.grow : this.grow;
            this.shrink = o.shrink != null ? o.shrink : this.shrink;
        }
    }

    /**
     * Get style with default values
     *
     * @return
     */
    @Override
    public void setUnassignedProperties() {
        if (this.direction == null) this.direction = FlexDirection.getDefault();
        if (this.wrap == null) this.wrap = FlexWrap.getDefault();
        if (this.basis == null) this.basis = DisplayUnit.builder().value(0f).type(DisplayUnit.Type.AUTO).build();
        if (this.grow == null) this.grow = DisplayUnit.builder().value(0f).type(DisplayUnit.Type.VALUE).build();
        if (this.shrink == null) this.shrink = DisplayUnit.builder().value(1f).type(DisplayUnit.Type.VALUE).build();
    }

    /**
     * Clone property
     *
     * @return
     */
    @Override
    public StyleProperty clone() {
        return FlexStyle.builder()
                .direction(direction)
                .wrap(wrap)
                .basis(basis != null ? basis.clone() : null)
                .grow(grow != null ? grow.clone() : null)
                .shrink(shrink != null ? shrink.clone() : null)
                .build();
    }

    /**
     * Get style name (identifier)
     *
     * @return
     */
    @Override
    public String getName() {
        return NAME;
    }
}
