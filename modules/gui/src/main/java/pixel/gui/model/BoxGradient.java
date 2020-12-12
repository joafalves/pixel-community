/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.gui.model;

import lombok.*;
import pixel.graphics.Color;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoxGradient {

    private Color startColor;
    private Color endColor;
    private Float feather;
    private Float radius;

    /**
     * Constructor
     *
     * @param o
     */
    public BoxGradient(BoxGradient o) {
        if (o.startColor != null) this.startColor = new Color(o.startColor);
        if (o.endColor != null) this.endColor = new Color(o.endColor);
        if (o.feather != null) this.feather = o.feather;
        if (o.radius != null) this.radius = o.radius;
    }
}
