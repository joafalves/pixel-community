/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.physics.shape;

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

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
