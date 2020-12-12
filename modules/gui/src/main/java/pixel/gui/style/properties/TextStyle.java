/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.properties;

import lombok.*;
import pixel.commons.model.HorizontalAlignment;
import pixel.commons.model.VerticalAlignment;
import pixel.gui.style.StyleProperty;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class TextStyle extends StyleProperty {

    private static final String NAME = "textStyle";

    private HorizontalAlignment horizontalAlignment;
    private VerticalAlignment verticalAlignment;

    /**
     * Merge property into existing values (when applicable)
     *
     * @param other
     */
    @Override
    public void merge(StyleProperty other) {
        if (other instanceof TextStyle) {
            var o = (TextStyle) other;
            this.horizontalAlignment = o.horizontalAlignment != null ? o.horizontalAlignment : this.horizontalAlignment;
            this.verticalAlignment = o.verticalAlignment != null ? o.verticalAlignment : this.verticalAlignment;
        }
    }

    /**
     * Set missing property values
     *
     * @return
     */
    @Override
    public void setUnassignedProperties() {
        if (this.horizontalAlignment == null) this.horizontalAlignment = HorizontalAlignment.LEFT;
        if (this.verticalAlignment == null) this.verticalAlignment = VerticalAlignment.TOP;
    }

    @Override
    public StyleProperty clone() {
        return TextStyle.builder()
                .horizontalAlignment(horizontalAlignment)
                .verticalAlignment(verticalAlignment)
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
