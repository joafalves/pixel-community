/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.math;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertTrue(triangle.overlapsWith(new Polygon(a.getVertices())));
        Assert.assertFalse(triangle.overlapsWith(new Polygon(b.getVertices())));

        // small pentagon collision
        Assert.assertTrue(smallPentagon.overlapsWith(new Polygon(a.getVertices())));
        Assert.assertFalse(smallPentagon.overlapsWith(new Polygon(b.getVertices())));

        // large pentagon collision
        Assert.assertTrue(largePentagon.overlapsWith(new Polygon(a.getVertices())));
        Assert.assertTrue(largePentagon.overlapsWith(new Polygon(b.getVertices())));
    }
}
