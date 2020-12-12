/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.physics.shape;

public class Circle extends Shape {

    private float radius;

    /**
     * Constructor
     *
     * @param radius
     */
    public Circle(float radius) {
        this.radius = radius;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
