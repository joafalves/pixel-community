package org.pixel.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MathHelperTest {

    @Test
    public void testLineIntersection() {
        Line lineA = new Line(new Vector2(0, 0), new Vector2(10, 10));
        Line lineB = new Line(new Vector2(10, 10), new Vector2(20, 20));
        Line lineC = new Line(new Vector2(0, 5), new Vector2(10, 5));

        Assertions.assertNull(MathHelper.intersect(lineA, lineB));
        Assertions.assertNotNull(MathHelper.intersect(lineA, lineC));
        Assertions.assertEquals(new Vector2(5, 5), MathHelper.intersect(lineA, lineC));
    }

    @Test
    public void testPolygonIntersection() {
        Line lineA = new Line(new Vector2(0, 0), new Vector2(10, 10));
        Polygon polygon = new Polygon(new Vector2(2, 2), new Vector2(2, 8),
                new Vector2(8, 8), new Vector2(8, 2));

        Assertions.assertNotNull(MathHelper.intersect(lineA, polygon));
        Assertions.assertEquals(2, MathHelper.intersect(lineA, polygon).size());
    }

    @Test
    public void linearInterpolationTest() {
        Assertions.assertEquals(5, MathHelper.linearInterpolation(0, 10, 0.5f));
        Assertions.assertEquals(5, MathHelper.linearInterpolation((byte) 0, (byte) 10, 0.5f));
    }
}
