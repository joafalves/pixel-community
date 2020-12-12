/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.properties;

import lombok.*;
import pixel.gui.style.StyleProperty;
import pixel.gui.style.model.DisplayUnit;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class SizeStyle extends StyleProperty {

    private static final String NAME = "sizeStyle";

    private DisplayUnit width;
    private DisplayUnit height;

    /**
     * Merge property into existing values (when applicable)
     *
     * @param other
     */
    @Override
    public void merge(StyleProperty other) {
        if (other instanceof SizeStyle) {
            var o = (SizeStyle) other;
            this.width = o.width != null ? o.width : this.width;
            this.height = o.height != null ? o.height : this.height;
        }
    }

    /**
     * Set missing property values
     *
     * @return
     */
    @Override
    public void setUnassignedProperties() {
        if (this.width == null) this.width = DisplayUnit.builder().value(0f).type(DisplayUnit.Type.PIXELS).build();
        if (this.height == null) this.height = DisplayUnit.builder().value(0f).type(DisplayUnit.Type.PIXELS).build();
    }

    @Override
    public StyleProperty clone() {
        return SizeStyle.builder()
                .width(width != null ? width.clone() : null)
                .height(height != null ? height.clone() : null)
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
