package org.pixel.math;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

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

    @Test
    public void overlapTest() {
        // Overlapping polygons
        List<Vector2> polygonA = Arrays.asList(
                new Vector2(0, 0),
                new Vector2(2, 0),
                new Vector2(1, 2)
        );

        List<Vector2> polygonB = Arrays.asList(
                new Vector2(1, 1),
                new Vector2(3, 1),
                new Vector2(2, 3)
        );

        Assertions.assertTrue(MathHelper.overlap(polygonA, polygonB), "Polygons should overlap");

        // Non-overlapping polygons
        List<Vector2> polygonC = Arrays.asList(
                new Vector2(0, 0),
                new Vector2(2, 0),
                new Vector2(1, 2)
        );

        List<Vector2> polygonD = Arrays.asList(
                new Vector2(3, 3),
                new Vector2(5, 3),
                new Vector2(4, 5)
        );

        Assertions.assertFalse(MathHelper.overlap(polygonC, polygonD), "Polygons should not overlap");
    }

}
