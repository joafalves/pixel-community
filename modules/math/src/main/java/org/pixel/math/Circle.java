/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.io.Serializable;

public class Circle implements Serializable {

    private Vector2 position;
    private float radius;

    /**
     * Constructor
     *
     * @param position
     * @param radius
     */
    public Circle(Vector2 position, float radius) {
        this.position = position;
        this.radius = radius;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
