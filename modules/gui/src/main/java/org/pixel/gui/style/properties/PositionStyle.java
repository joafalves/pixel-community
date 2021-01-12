/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.properties;

import lombok.*;
import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.model.DisplayUnit;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class PositionStyle extends StyleProperty {

    private static final String NAME = "positionStyle";

    private DisplayUnit left;
    private DisplayUnit right;
    private DisplayUnit top;
    private DisplayUnit bottom;

    /**
     * Merge property into existing values (when applicable)
     *
     * @param other
     */
    @Override
    public void merge(StyleProperty other) {
        if (other instanceof PositionStyle) {
            var o = (PositionStyle) other;
            this.left = o.left != null ? o.left : this.left;
            this.right = o.right != null ? o.right : this.right;
            this.top = o.top != null ? o.top : this.top;
            this.bottom = o.bottom != null ? o.bottom : this.bottom;
        }
    }

    /**
     * Get style with default values
     *
     * @return
     */
    @Override
    public void setUnassignedProperties() {
        if (this.left == null) this.left = DisplayUnit.builder().value(0f).type(DisplayUnit.Type.PIXELS).build();
        //if (this.right == null) this.right = DisplayUnit.builder().value(0f).type(DisplayUnit.Type.PIXELS).build();
        if (this.top == null) this.top = DisplayUnit.builder().value(0f).type(DisplayUnit.Type.PIXELS).build();
        //if (this.bottom == null) this.bottom = DisplayUnit.builder().value(0f).type(DisplayUnit.Type.PIXELS).build();
    }

    /**
     * Clone property
     *
     * @return
     */
    @Override
    public StyleProperty clone() {
        return PositionStyle.builder()
                .left(left != null ? left.clone() : null)
                .right(right != null ? right.clone() : null)
                .top(top != null ? top.clone() : null)
                .bottom(bottom != null ? bottom.clone() : null)
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
