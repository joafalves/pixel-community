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
public class Line implements Serializable {

    private float x1, y1;
    private float x2, y2;

    /**
     * Constructor.
     *
     * @param x1 The x coordinate of the first point.
     * @param y1 The y coordinate of the first point.
     * @param x2 The x coordinate of the second point.
     * @param y2 The y coordinate of the second point.
     */
    public Line(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * Constructor.
     *
     * @param point1 The first point.
     * @param point2 The second point.
     */
    public Line(Vector2 point1, Vector2 point2) {
        this(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    /**
     * Constructor.
     *
     * @param other The other line.
     */
    public Line(Line other) {
        this.x1 = other.x1;
        this.y1 = other.y1;
        this.x2 = other.x2;
        this.y2 = other.y2;
    }

    /**
     * Set the line values.
     *
     * @param point1 The first point.
     * @param point2 The second point.
     */
    public void set(Vector2 point1, Vector2 point2) {
        this.x1 = point1.getX();
        this.y1 = point1.getY();
        this.x2 = point2.getX();
        this.y2 = point2.getY();
    }

    /**
     * Set the line values.
     *
     * @param x1 The x coordinate of the first point.
     * @param y1 The y coordinate of the first point.
     * @param x2 The x coordinate of the second point.
     * @param y2 The y coordinate of the second point.
     */
    public void set(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Line) {
            Line line = (Line) obj;
            return this.x1 == line.x1 && this.y1 == line.y1 && this.x2 == line.x2 && this.y2 == line.y2;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s: [x1: %f, y1: %f, x2: %f, y2: %f]",
                this.getClass().getSimpleName(), getX1(), getY1(), getX2(), getY2());
    }
}
