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
public class FontStyle extends StyleProperty {

    private static final String NAME = "fontStyle";

    private Float fontSize;
    private String fontFamily;

    /**
     * Merge property into existing values (when applicable)
     *
     * @param other
     */
    @Override
    public void merge(StyleProperty other) {
        if (other instanceof FontStyle) {
            var o = (FontStyle) other;
            this.fontSize = o.fontSize != null ? o.fontSize : this.fontSize;
            this.fontFamily = o.fontFamily != null ? o.fontFamily : this.fontFamily;
        }
    }

    /**
     * Set missing property values
     *
     * @return
     */
    @Override
    public void setUnassignedProperties() {
        if (this.fontSize == null) this.fontSize = 14.f;
        if (this.fontFamily == null) this.fontFamily = "roboto";
    }

    @Override
    public StyleProperty clone() {
        return FontStyle.builder()
                .fontFamily(fontFamily)
                .fontSize(fontSize)
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
