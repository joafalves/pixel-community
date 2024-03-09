/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.physics.shape;

import lombok.Getter;
import lombok.Setter;
import org.pixel.math.Vector2;

@Getter
@Setter
public class PolygonShape extends Shape {

    private final Vector2[] vertices;

    public PolygonShape(Vector2[] vertices) {
        this.vertices = vertices;
    }

    public Vector2[] getAxes() {
        Vector2[] axes = new Vector2[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            Vector2 p1 = vertices[i];
            Vector2 p2 = vertices[i + 1 == vertices.length ? 0 : i + 1];
            Vector2 edge = Vector2.subtract(p1, p2);
            Vector2 normal = edge.rightNormal();
            normal.normalize();
            axes[i] = normal;
        }
        return axes;
    }

    public Vector2 getCenter() {
        float centerX = 0;
        float centerY = 0;
        for (Vector2 v : vertices) {
            centerX += v.getX();
            centerY += v.getY();
        }
        centerX /= vertices.length;
        centerY /= vertices.length;
        return new Vector2(centerX, centerY);
    }

}
