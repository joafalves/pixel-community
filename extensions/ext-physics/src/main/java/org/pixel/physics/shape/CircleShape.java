/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.physics.shape;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CircleShape extends Shape {

    private float radius;

    /**
     * Constructor
     *
     * @param radius
     */
    public CircleShape(float radius) {
        this.radius = radius;
    }

}
