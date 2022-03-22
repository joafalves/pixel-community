/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PolygonTest {

    @Test
    public void testCollision() {
        Boundary a, b;
        a = new Boundary(Vector2.zero(), new Vector2(10, 0), new Vector2(0, 10), new Vector2(10, 10));
        b = new Boundary(new Vector2(15, 15), new Vector2(20, 15), new Vector2(15, 20), new Vector2(20, 20));

        Polygon triangle = new Polygon(new Vector2(0, 0), new Vector2(5, 5), new Vector2(0, 5));
        Polygon smallPentagon = new Polygon(
                new Vector2(0, 2),
                new Vector2(4, 0),
                new Vector2(4, 2),
                new Vector2(3, 4),
                new Vector2(1, 4));
        Polygon largePentagon = new Polygon(
                new Vector2(0, 20),
                new Vector2(40, 0),
                new Vector2(40, 20),
                new Vector2(30, 40),
                new Vector2(10, 40));

        // triangle collision
        Assertions.assertTrue(triangle.overlapsWith(new Polygon(a.getVertices())));
        Assertions.assertFalse(triangle.overlapsWith(new Polygon(b.getVertices())));

        // small pentagon collision
        Assertions.assertTrue(smallPentagon.overlapsWith(new Polygon(a.getVertices())));
        Assertions.assertFalse(smallPentagon.overlapsWith(new Polygon(b.getVertices())));

        // large pentagon collision
        Assertions.assertTrue(largePentagon.overlapsWith(new Polygon(a.getVertices())));
        Assertions.assertTrue(largePentagon.overlapsWith(new Polygon(b.getVertices())));
    }

    @Test
    public void equalsTest() {
        Polygon polygonA = new Polygon(
                new Vector2(0, 10),
                new Vector2(10, 10),
                new Vector2(5, 0));
        Polygon polygonB = new Polygon(
                new Vector2(0, 10),
                new Vector2(10, 10),
                new Vector2(5, 0));
        Polygon polygonC = new Polygon(
                new Vector2(10, 20),
                new Vector2(20, 20),
                new Vector2(15, 10));

        Assertions.assertEquals(polygonA, polygonB);
        Assertions.assertNotEquals(polygonA, polygonC);
    }
}
