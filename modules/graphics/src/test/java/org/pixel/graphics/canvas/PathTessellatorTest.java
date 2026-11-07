package org.pixel.graphics.canvas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathTessellatorTest {

    private static float triangulatedArea(PathTessellator t, float[] xy, int count) {
        int n = t.triangulate(xy, 0, count);
        float area = 0;
        for (int i = 0; i < n; i++) {
            int a = t.triangle(i, 0), b = t.triangle(i, 1), c = t.triangle(i, 2);
            float[] tri = {xy[a * 2], xy[a * 2 + 1], xy[b * 2], xy[b * 2 + 1], xy[c * 2], xy[c * 2 + 1]};
            area += Math.abs(PathTessellator.signedArea(tri, 0, 3));
        }
        return area;
    }

    @Test
    void convexPolygonInBothWindings() {
        PathTessellator t = new PathTessellator();
        float[] square = {0, 0, 10, 0, 10, 10, 0, 10};
        float[] reversed = {0, 10, 10, 10, 10, 0, 0, 0};
        assertEquals(2, t.triangulate(square, 0, 4));
        assertEquals(100f, triangulatedArea(t, square, 4), 1e-3);
        assertEquals(100f, triangulatedArea(t, reversed, 4), 1e-3);
    }

    @Test
    void concavePolygonsAreCoveredExactly() {
        PathTessellator t = new PathTessellator();
        // L shape (area 300) and a 5-point star
        float[] l = {0, 0, 10, 0, 10, 20, 20, 20, 20, 30, 0, 30};
        assertEquals(4, t.triangulate(l, 0, 6));
        assertEquals(Math.abs(PathTessellator.signedArea(l, 0, 6)), triangulatedArea(t, l, 6), 1e-3);

        float[] star = new float[20];
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5;
            float r = i % 2 == 0 ? 50 : 20;
            star[i * 2] = (float) (Math.cos(a) * r);
            star[i * 2 + 1] = (float) (Math.sin(a) * r);
        }
        assertEquals(8, t.triangulate(star, 0, 10));
        assertEquals(Math.abs(PathTessellator.signedArea(star, 0, 10)), triangulatedArea(t, star, 10), 1e-2);
    }

    @Test
    void collinearAndDuplicatedVerticesDoNotStall() {
        PathTessellator t = new PathTessellator();
        float[] withCollinear = {0, 0, 5, 0, 10, 0, 10, 10, 0, 10};
        assertEquals(100f, triangulatedArea(t, withCollinear, 5), 1e-3);
    }

    @Test
    void offsetRangesAreRespected() {
        PathTessellator t = new PathTessellator();
        float[] data = {99, 99, 99, 99, 0, 0, 4, 0, 4, 4};
        assertEquals(1, t.triangulate(data, 2, 3));
        assertTrue(t.triangle(0, 0) < 3 && t.triangle(0, 1) < 3 && t.triangle(0, 2) < 3);
    }

    @Test
    void flatteningStaysWithinTolerance() {
        float tolerance = 0.25f;
        float x0 = 0, y0 = 0, c1x = 50, c1y = 200, c2x = 250, c2y = -150, x1 = 300, y1 = 50;
        int n = PathTessellator.cubicSegments(x0, y0, c1x, c1y, c2x, c2y, x1, y1, tolerance);
        assertTrue(n > 4 && n < 200, "segments " + n);
        // every chord midpoint stays close to the curve
        float maxError = 0;
        for (int i = 0; i < n; i++) {
            float t0 = i / (float) n, t1 = (i + 1) / (float) n, tm = (t0 + t1) / 2;
            float[] a = cubic(x0, y0, c1x, c1y, c2x, c2y, x1, y1, t0);
            float[] b = cubic(x0, y0, c1x, c1y, c2x, c2y, x1, y1, t1);
            float[] m = cubic(x0, y0, c1x, c1y, c2x, c2y, x1, y1, tm);
            maxError = Math.max(maxError, (float) Math.hypot(m[0] - (a[0] + b[0]) / 2, m[1] - (a[1] + b[1]) / 2));
        }
        assertTrue(maxError <= tolerance, "max chord error " + maxError);
        assertEquals(1, PathTessellator.quadSegments(0, 0, 5, 0, 10, 0, tolerance)); // straight: one segment
        assertTrue(PathTessellator.arcSegments(100, (float) (2 * Math.PI), tolerance) >= 40);
    }

    private static float[] cubic(float x0, float y0, float c1x, float c1y, float c2x, float c2y, float x1, float y1,
                                 float t) {
        float mt = 1 - t;
        float a = mt * mt * mt, b = 3 * mt * mt * t, c = 3 * mt * t * t, d = t * t * t;
        return new float[]{a * x0 + b * c1x + c * c2x + d * x1, a * y0 + b * c1y + c * c2y + d * y1};
    }
}
