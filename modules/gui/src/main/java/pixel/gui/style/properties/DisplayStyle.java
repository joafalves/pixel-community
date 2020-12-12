/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.properties;

import lombok.*;
import pixel.gui.style.StyleProperty;
import pixel.gui.style.model.DisplayType;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class DisplayStyle extends StyleProperty {

    private static final String NAME = "displayStyle";

    private DisplayType type;

    /**
     * Merge property into existing values (when applicable)
     *
     * @param other
     */
    @Override
    public void merge(StyleProperty other) {
        if (other instanceof DisplayStyle) {
            var o = (DisplayStyle) other;
            this.type = o.type != null ? o.type : this.type;
        }
    }

    /**
     * Get style with default values
     *
     * @return
     */
    @Override
    public void setUnassignedProperties() {
        if (this.type == null) this.type = DisplayType.BLOCK;
    }

    /**
     * Clone property
     *
     * @return
     */
    @Override
    public StyleProperty clone() {
        return DisplayStyle.builder()
                .type(type)
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
