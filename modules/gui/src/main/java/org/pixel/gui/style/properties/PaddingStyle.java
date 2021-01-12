/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.properties;

import lombok.*;
import org.pixel.gui.style.StyleProperty;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class PaddingStyle extends StyleProperty {

    private static final String NAME = "paddingStyle";

    private Float left;
    private Float right;
    private Float top;
    private Float bottom;

    /**
     * Merge property into existing values (when applicable)
     *
     * @param other
     */
    @Override
    public void merge(StyleProperty other) {
        if (other instanceof PaddingStyle) {
            var o = (PaddingStyle) other;
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
        if (this.left == null) this.left = 0f;
        if (this.right == null) this.right = 0f;
        if (this.top == null) this.top = 0f;
        if (this.bottom == null) this.bottom = 0f;
    }

    /**
     * Clone property
     *
     * @return
     */
    @Override
    public StyleProperty clone() {
        return PaddingStyle.builder()
                .left(left)
                .right(right)
                .top(top)
                .bottom(bottom)
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
