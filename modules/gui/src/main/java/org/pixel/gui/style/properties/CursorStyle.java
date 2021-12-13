/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.properties;

import lombok.*;
import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.model.CursorType;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CursorStyle extends StyleProperty {

    private static final String NAME = "cursorStyle";

    private CursorType type;

    /**
     * Merge property into existing values (when applicable)
     *
     * @param other
     */
    @Override
    public void merge(StyleProperty other) {
        if (other instanceof CursorStyle) {
            var o = (CursorStyle) other;
            this.type = o.type != null ? o.type : this.type;
        }
    }

    /**
     * Set missing property values
     */
    @Override
    public void setUnassignedProperties() {
        if (this.type == null) this.type = CursorType.getDefault();
    }

    @Override
    public StyleProperty clone() {
        return CursorStyle.builder()
                .type(type)
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
