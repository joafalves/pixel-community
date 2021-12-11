/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MathHelper {

    //region properties

    public static final float E = (float) Math.E;
    public static final float PI = (float) Math.PI;
    public static final float PI2 = (float) Math.PI * 2.0f;
    public static final float PI4 = (float) Math.PI * 4.0f;
    public static final float PIo2 = (float) Math.PI / 2.0f;
    public static final float PIo4 = (float) Math.PI / 4.0f;
    public static final float EPSILON = 0.0001f;

    //endregion

    //region public static methods

    /**
     * @param value
     * @return
     */
    public static float tan(float value) {
        return (float) Math.tan(value);
    }

    /**
     * @param value
     * @return
     */
    public static float sin(float value) {
        return (float) Math.sin(value);
    }

    /**
     * @param value
     * @return
     */
    public static float cos(float value) {
        return (float) Math.cos(value);
    }

    /**
     * @param value
     * @return
     */
    public static float acos(float value) {
        return (float) Math.acos(value);
    }

    /**
     * @param y
     * @param x
     * @return
     */
    public static float atan2(float y, float x) {
        return (float) Math.atan2(y, x);
    }

    /**
     * Generates a random integer number between two numbers
     *
     * @param min
     * @param max
     * @return
     */
    public static int random(int min, int max) {
        return ThreadLocalRandom.current().nextInt(max - min) + min;
    }

    /**
     * Generates a random float number between two numbers
     *
     * @param min
     * @param max
     * @return
     */
    public static float random(float min, float max) {
        return min + ThreadLocalRandom.current().nextFloat() * (max - min);
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static float min(float a, float b) {
        return Math.min(a, b);
    }

    /**
     * @param values
     * @return
     */
    public static float max(float... values) {
        float max = Float.MIN_VALUE;
        for (float value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    /**
     * @param values
     * @return
     */
    public static float min(float... values) {
        float min = Float.MAX_VALUE;
        for (float value : values) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    /**
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static long clamp(long value, long min, long max) {
        return value < min ? min : value > max ? max : value;
    }

    /**
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static float clamp(float value, float min, float max) {
        return value < min ? min : value > max ? max : value;
    }

    /**
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static int clamp(int value, int min, int max) {
        return value < min ? min : value > max ? max : value;
    }

    /**
     * @param degree
     * @return
     */
    public static float toRadians(float degree) {
        return degree * 0.0174532925f;
    }

    /**
     * @param radian
     * @return
     */
    public static float toDegrees(float radian) {
        return radian * 57.295779513f;
    }

    /**
     * Linear interpolation between two points
     *
     * @param a point A
     * @param b point B
     * @param t amount of interpolation
     * @return
     */
    public static byte linearInterpolation(byte a, byte b, float t) {
        return (byte) (a + (b - a) * t);
    }

    /**
     * Linear interpolation between two points
     *
     * @param a point A
     * @param b point B
     * @param t amount of interpolation
     * @return
     */
    public static float linearInterpolation(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /**
     * Rotates a point around a given origin
     *
     * @param point
     * @param origin
     * @param angle
     * @return
     */
    public static Vector2 rotatePoint(Vector2 point, Vector2 origin, float angle) {
        return rotatePoint(point.getX(), point.getY(), origin.getX(), origin.getY(), angle);
    }

    /**
     * Rotates a point around a given origin
     *
     * @param x       point x
     * @param y       point y
     * @param originX origin point x
     * @param originY origin point y
     * @param angle   angle in radians
     * @return
     */
    public static Vector2 rotatePoint(float x, float y, float originX, float originY, float angle) {
        return new Vector2(originX + (x - originX) * cos(angle) - (y - originY) * sin(angle),
                originY + (y - originY) * cos(angle) + (x - originX) * sin(angle));
    }

    /**
     * Calculates the resulting direction for a given target based on the current origin
     *
     * @param origin
     * @param target
     * @return
     */
    public static float direction(Vector2 origin, Vector2 target) {
        return direction(origin.getX(), origin.getY(), target.getX(), target.getY());
    }

    /**
     * Calculates the resulting direction for a given target based on the current origin
     *
     * @param originX
     * @param originY
     * @param targetX
     * @param targetY
     * @return
     */
    public static float direction(float originX, float originY, float targetX, float targetY) {
        return (float) Math.atan2(targetY - originY, targetX - originX);
    }

    /**
     * Checks for polygon collision (vertices are required to be ordered either clockwise or counter-clockwise). This
     * approach is based on the SAT (Separating Axis Theorem).
     *
     * @param polygonVerticesA
     * @param polygonVerticesB
     * @return
     */
    public static boolean overlap(List<Vector2> polygonVerticesA, List<Vector2> polygonVerticesB) {
        if (polygonVerticesA.size() < 3 || polygonVerticesB.size() < 3) {
            throw new RuntimeException("A polygon requires at least three vertices");
        }

        // the following collision detection is based on the separating axis theorem:
        // http://www.gamedev.net/page/resources/_/technical/game-programming/2d-rotated-rectangle-collision-r2604
        List<Vector2> vertices = new ArrayList<>();
        vertices.addAll(polygonVerticesA);
        vertices.addAll(polygonVerticesB);

        for (int i1 = 0; i1 < vertices.size(); ++i1) {
            int i2 = (i1 + 1) % vertices.size();
            Vector2 p1 = vertices.get(i1);
            Vector2 p2 = vertices.get(i2);
            Vector2 norm = new Vector2(p2);
            norm.subtract(p1);

            // get min/max for 'a'
            Float minA = null, maxA = null;
            for (Vector2 v : polygonVerticesA) {
                float projected = norm.getX() * v.getX() + norm.getY() * v.getY();
                if (minA == null || projected < minA) {
                    minA = projected;
                }
                if (maxA == null || projected > maxA) {
                    maxA = projected;
                }
            }

            // get min/max for 'b'
            Float minB = null, maxB = null;
            for (Vector2 v : polygonVerticesB) {
                float projected = norm.getX() * v.getX() + norm.getY() * v.getY();
                if (minB == null || projected < minB) {
                    minB = projected;
                }
                if (maxB == null || projected > maxB) {
                    maxB = projected;
                }
            }

            if (maxA < minB || maxB < minA) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks for line intersection against a polygon. Returns the intersection point(s) if any.
     *
     * @param line
     * @param polygon
     * @return
     */
    public static List<Vector2> intersect(Line line, Polygon polygon) {
        List<Vector2> intersections = null;
        List<Vector2> vertices = polygon.getVertices();
        for (int i = 0; i < polygon.getVertices().size() - 1; i++) {
            Vector2 result = intersect(line.getX1(), line.getY1(), line.getX2(), line.getY2(),
                    vertices.get(i).getX(), vertices.get(i).getY(), vertices.get(i + 1).getX(),
                    vertices.get(i + 1).getY());
            if (result != null) {
                if (intersections == null) { // only create list if necessary
                    intersections = new ArrayList<>();
                }

                if (!intersections.contains(result)) {
                    intersections.add(result);
                }
            }
        }

        return intersections;
    }

    /**
     * Checks for line intersection and returns a point of intersection if found.
     *
     * @param a
     * @param b
     * @return
     */
    public static Vector2 intersect(Line a, Line b) {
        if (a == null || b == null || a.equals(b)) {
            return null;
        }

        return intersect(a.getX1(), a.getY1(), a.getX2(), a.getY2(), b.getX1(), b.getY1(), b.getX2(), b.getY2());
    }

    /**
     * Checks for line intersection and returns a point of intersection if found.
     *
     * @param p1x1
     * @param p1y1
     * @param p1x2
     * @param p1y2
     * @param p2x1
     * @param p2y1
     * @param p2x2
     * @param p2y2
     * @return
     */
    public static Vector2 intersect(float p1x1, float p1y1, float p1x2, float p1y2,
            float p2x1, float p2y1, float p2x2, float p2y2) {
        float d = (p1x1 - p1x2) * (p2y1 - p2y2) - (p1y1 - p1y2) * (p2x1 - p2x2);
        if (d == 0) {
            return null;
        }

        float xi = ((p2x1 - p2x2) * (p1x1 * p1y2 - p1y1 * p1x2) - (p1x1 - p1x2) * (p2x1 * p2y2 - p2y1 * p2x2)) / d;
        float yi = ((p2y1 - p2y2) * (p1x1 * p1y2 - p1y1 * p1x2) - (p1y1 - p1y2) * (p2x1 * p2y2 - p2y1 * p2x2)) / d;

        return new Vector2(xi, yi);
    }

    //endregion
}
