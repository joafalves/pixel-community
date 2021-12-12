/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    /**
     * Constructor
     *
     * @param other
     */
    public Circle(Circle other) {
        this.position = new Vector2(other.getPosition());
        this.radius = other.getRadius();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Circle) {
            return ((Circle) obj).getPosition().equals(getPosition()) && ((Circle) obj).getRadius() == this.getRadius();
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s: [pos: '%s', rad: %f]",
                this.getClass().getSimpleName(), getPosition().toString(), getRadius());
    }
}
