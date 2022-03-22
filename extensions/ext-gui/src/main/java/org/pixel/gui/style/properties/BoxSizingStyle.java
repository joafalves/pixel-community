/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.style.properties;

import lombok.*;
import org.pixel.gui.style.StyleProperty;
import org.pixel.gui.style.model.BoxSizingType;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class BoxSizingStyle extends StyleProperty {

    private static final String NAME = "boxSizingStyle";

    private BoxSizingType type;

    /**
     * Merge property into existing values (when applicable)
     *
     * @param other
     */
    @Override
    public void merge(StyleProperty other) {
        if (other instanceof BoxSizingStyle) {
            var o = (BoxSizingStyle) other;
            this.type = o.type != null ? o.type : this.type;
        }
    }

    /**
     * Set missing property values
     */
    @Override
    public void setUnassignedProperties() {
        if (this.type == null) this.type = BoxSizingType.getDefault();
    }

    /**
     * @return
     */
    @Override
    public StyleProperty clone() {
        return BoxSizingStyle.builder()
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
