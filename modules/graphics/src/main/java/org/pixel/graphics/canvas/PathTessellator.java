package org.pixel.graphics.canvas;

import java.util.Arrays;

/**
 * GL-free path geometry: curve flattening and polygon triangulation. Reuses its scratch arrays (allocation-free once
 * warmed up). Not thread-safe.
 */
final class PathTessellator {

    /**
     * Segments for a quadratic Bezier so that the polyline stays within {@code tolerance} of the curve (Wang's
     * formula).
     */
    static int quadSegments(float x0, float y0, float cx, float cy, float x1, float y1, float tolerance) {
        float dx = x0 - 2 * cx + x1, dy = y0 - 2 * cy + y1;
        float dd = (float) Math.sqrt(dx * dx + dy * dy);
        return clampSegments((int) Math.ceil(Math.sqrt(dd / (4f * tolerance))));
    }

    /**
     * Segments for a cubic Bezier so that the polyline stays within {@code tolerance} of the curve (Wang's formula).
     */
    static int cubicSegments(float x0, float y0, float c1x, float c1y, float c2x, float c2y, float x1, float y1,
                             float tolerance) {
        float ax = x0 - 2 * c1x + c2x, ay = y0 - 2 * c1y + c2y;
        float bx = c1x - 2 * c2x + x1, by = c1y - 2 * c2y + y1;
        float dd = (float) Math.sqrt(Math.max(ax * ax + ay * ay, bx * bx + by * by));
        return clampSegments((int) Math.ceil(Math.sqrt(3f * dd / (4f * tolerance))));
    }

    /**
     * Segments for a circular arc so that the chord error stays within {@code tolerance}.
     */
    static int arcSegments(float radius, float sweep, float tolerance) {
        if (radius <= tolerance) {
            return clampSegments((int) Math.ceil(Math.abs(sweep) / (Math.PI / 2)));
        }
        double step = 2 * Math.acos(1 - tolerance / radius);
        return clampSegments((int) Math.ceil(Math.abs(sweep) / step));
    }

    private static int clampSegments(int n) {
        return Math.max(1, Math.min(n, 512));
    }

    // region triangulation (ear clipping)

    private int[] prev = new int[64];
    private int[] next = new int[64];
    private int[] triangles = new int[64 * 3];
    private int triangleCount;

    /**
     * Triangulate a simple polygon (convex or concave, either winding, no holes, no self-intersections).
     * Self-intersecting input produces a partial result instead of looping.
     *
     * @param xy     Vertex coordinates (x0, y0, x1, y1, ...).
     * @param offset Index of the first vertex (in vertices, not floats).
     * @param count  Number of vertices.
     * @return The number of triangles; read them with {@link #triangle(int, int)}.
     */
    int triangulate(float[] xy, int offset, int count) {
        triangleCount = 0;
        if (count < 3) {
            return 0;
        }
        ensure(count);
        for (int i = 0; i < count; i++) {
            prev[i] = i == 0 ? count - 1 : i - 1;
            next[i] = i == count - 1 ? 0 : i + 1;
        }
        boolean ccw = signedArea(xy, offset, count) > 0; // y down: positive = clockwise on screen

        int remaining = count;
        int current = 0;
        int guard = 0;
        while (remaining > 3 && guard < remaining * remaining + 10) {
            int a = prev[current], b = current, c = next[current];
            if (isEar(xy, offset, a, b, c, ccw)) {
                addTriangle(a, b, c);
                next[a] = c;
                prev[c] = a;
                remaining--;
                current = c;
                guard = 0;
            } else {
                current = next[current];
                guard++;
            }
        }
        if (remaining == 3) {
            addTriangle(prev[current], current, next[current]);
        }
        return triangleCount;
    }

    /**
     * @param triangle Triangle index.
     * @param corner   0..2.
     * @return The polygon vertex index (relative to the triangulated range).
     */
    int triangle(int triangle, int corner) {
        return triangles[triangle * 3 + corner];
    }

    /**
     * Signed area (shoelace) of a polygon; positive when the points run clockwise on screen (y axis down).
     */
    static float signedArea(float[] xy, int offset, int count) {
        double area = 0;
        for (int i = 0; i < count; i++) {
            int j = i == count - 1 ? 0 : i + 1;
            float xi = xy[(offset + i) * 2], yi = xy[(offset + i) * 2 + 1];
            float xj = xy[(offset + j) * 2], yj = xy[(offset + j) * 2 + 1];
            area += (double) xi * yj - (double) xj * yi;
        }
        return (float) (area * 0.5);
    }

    private boolean isEar(float[] xy, int offset, int a, int b, int c, boolean ccw) {
        float ax = xy[(offset + a) * 2], ay = xy[(offset + a) * 2 + 1];
        float bx = xy[(offset + b) * 2], by = xy[(offset + b) * 2 + 1];
        float cx = xy[(offset + c) * 2], cy = xy[(offset + c) * 2 + 1];
        float cross = (bx - ax) * (cy - ay) - (by - ay) * (cx - ax);
        if (ccw ? cross <= 0 : cross >= 0) {
            return false; // reflex (or degenerate) vertex
        }
        for (int p = next[c]; p != a; p = next[p]) {
            float px = xy[(offset + p) * 2], py = xy[(offset + p) * 2 + 1];
            if ((px == ax && py == ay) || (px == bx && py == by) || (px == cx && py == cy)) {
                continue; // duplicated vertex: not an obstacle
            }
            if (pointInTriangle(px, py, ax, ay, bx, by, cx, cy)) {
                return false;
            }
        }
        return true;
    }

    private static boolean pointInTriangle(float px, float py, float ax, float ay, float bx, float by, float cx,
                                           float cy) {
        float d1 = (px - bx) * (ay - by) - (ax - bx) * (py - by);
        float d2 = (px - cx) * (by - cy) - (bx - cx) * (py - cy);
        float d3 = (px - ax) * (cy - ay) - (cx - ax) * (py - ay);
        // strictly inside: points on an edge (collinear vertices) don't block the ear
        return (d1 < 0 && d2 < 0 && d3 < 0) || (d1 > 0 && d2 > 0 && d3 > 0);
    }

    private void addTriangle(int a, int b, int c) {
        int i = triangleCount * 3;
        if (i + 3 > triangles.length) {
            triangles = Arrays.copyOf(triangles, triangles.length * 2);
        }
        triangles[i] = a;
        triangles[i + 1] = b;
        triangles[i + 2] = c;
        triangleCount++;
    }

    private void ensure(int count) {
        if (count > prev.length) {
            int capacity = Math.max(count, prev.length * 2);
            prev = new int[capacity];
            next = new int[capacity];
        }
        if (count * 3 > triangles.length) {
            triangles = Arrays.copyOf(triangles, count * 3);
        }
    }

    // endregion
}
