/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Polygon implements Serializable {

    //region private properties

    private List<Vector2> vertices;

    //endregion

    //region constructors

    /**
     * @param vertices
     */
    public Polygon(Vector2... vertices) {
        if (vertices.length < 3) {
            throw new RuntimeException("A polygon requires at least three vertices");
        }

        this.vertices = new ArrayList<>(Arrays.asList(vertices));
    }

    /**
     * @param vertices
     */
    public Polygon(List<Vector2> vertices) {
        if (vertices.size() < 3) {
            throw new RuntimeException("A polygon requires at least three vertices");
        }

        this.vertices = vertices;
    }

    //endregion

    //region public methods

    /**
     * Tests polygon is overlapping another (vertices are required to be ordered either clockwise or counter-clockwise)
     *
     * @param other
     * @return
     */
    public boolean overlapsWith(Polygon other) {
        return overlap(this, other);
    }

    //endregion

    //region public static methods

    /**
     * Tests if two polygons are overlapping each other
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean overlap(Polygon a, Polygon b) {
        return MathHelper.overlap(a.vertices, b.vertices);
    }

    //endregion

    //region getters & setters

    public List<Vector2> getVertices() {
        return vertices;
    }

    public void setVertices(List<Vector2> vertices) {
        this.vertices = vertices;
    }

    //endregion
}
