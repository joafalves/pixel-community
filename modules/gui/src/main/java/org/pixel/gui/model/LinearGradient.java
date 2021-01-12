/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.gui.model;

import lombok.*;
import org.pixel.graphics.Color;
import org.pixel.math.Vector2;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LinearGradient {

    private Color startColor;
    private Color endColor;
    private Vector2 startPosition;
    private Vector2 endPosition;

    /**
     * Constructor
     *
     * @param o
     */
    public LinearGradient(LinearGradient o) {
        if (o.startColor != null) this.startColor = new Color(o.startColor);
        if (o.endColor != null) this.endColor = new Color(o.endColor);
        if (o.startPosition != null) this.startPosition = new Vector2(o.startPosition);
        if (o.endPosition != null) this.endPosition = new Vector2(o.endPosition);
    }

}
