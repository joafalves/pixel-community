/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.style.properties;

import lombok.*;
import pixel.graphics.Color;
import pixel.gui.style.StyleProperty;
import pixel.gui.style.model.ShadowType;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class BoxShadowStyle extends StyleProperty {

    private static final String NAME = "boxShadowStyle";

    private Color color;
    private Float horizontalOffset;
    private Float verticalOffset;
    private Float blur;
    private Float spread;
    private ShadowType shadowType;

    /**
     * Merge property into existing values (when applicable)
     *
     * @param other
     */
    @Override
    public void merge(StyleProperty other) {
        if (other instanceof BoxShadowStyle) {
            var o = (BoxShadowStyle) other;
            this.color = o.color != null ? o.color : this.color;
            this.horizontalOffset = o.horizontalOffset != null ? o.horizontalOffset : this.horizontalOffset;
            this.verticalOffset = o.verticalOffset != null ? o.verticalOffset : this.verticalOffset;
            this.blur = o.blur != null ? o.blur : this.blur;
            this.spread = o.spread != null ? o.spread : this.spread;
            this.shadowType = o.shadowType != null ? o.shadowType : this.shadowType;
        }
    }

    /**
     * Set missing property values
     *
     * @return
     */
    @Override
    public void setUnassignedProperties() {
        if (this.color == null) this.color = Color.BLACK;
        if (this.horizontalOffset == null) this.horizontalOffset = 0f;
        if (this.verticalOffset == null) this.verticalOffset = 0f;
        if (this.blur == null) this.blur = 0f;
        if (this.spread == null) this.spread = 0f;
        if (this.shadowType == null) this.shadowType = ShadowType.getDefault();
    }

    @Override
    public StyleProperty clone() {
        return BoxShadowStyle.builder()
                .color(new Color(color))
                .horizontalOffset(horizontalOffset)
                .verticalOffset(verticalOffset)
                .blur(blur)
                .spread(spread)
                .shadowType(shadowType)
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
