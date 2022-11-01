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
     * Returns the value of the first argument raised to the power of the second argument.
     *
     * @param value The base value.
     * @param power The exponent.
     * @return The value of the base raised to the power of the exponent.
     */
    public static float pow(float value, float power) {
        return (float) StrictMath.pow(value, power);
    }

    /**
     * Returns the correctly rounded positive square root of a float value.
     *
     * @param value The value.
     * @return The square root of the value.
     */
    public static float sqrt(float value) {
        return (float) StrictMath.sqrt(value);
    }

    /**
     * Returns the largest (closest to positive infinity) double value that is less than or equal to the argument and is
     * equal to a mathematical integer. Special cases:
     *
     * @param value The value.
     * @return The largest integer less than or equal to the argument.
     */
    public static float floor(float value) {
        return (float) StrictMath.floor(value);
    }

    /**
     * Returns the smallest (closest to negative infinity) double value that is greater than or equal to the argument
     * and is equal to a mathematical integer. Special cases:
     *
     * @param value The value.
     * @return The smallest integer greater than or equal to the argument.
     */
    public static float ceil(float value) {
        return (float) StrictMath.ceil(value);
    }

    /**
     * Tangent function.
     *
     * @param value The value to calculate the tangent of.
     * @return The tangent of the value.
     */
    public static float tan(float value) {
        return (float) StrictMath.tan(value);
    }

    /**
     * Sinus function.
     *
     * @param value The value to calculate the sinus of (in radians).
     * @return The sinus of the value.
     */
    public static float sin(float value) {
        return (float) StrictMath.sin(value);
    }

    /**
     * Cosine function.
     *
     * @param value The value to calculate the cosine of (in radians).
     * @return The cosine of the value.
     */
    public static float cos(float value) {
        return (float) StrictMath.cos(value);
    }

    /**
     * Arc cosine function.
     *
     * @param value The value to calculate the arc cosine of (in radians).
     * @return The arc cosine of the value.
     */
    public static float acos(float value) {
        return (float) StrictMath.acos(value);
    }

    /**
     * Arc Tangent function (inverted).
     *
     * @param y The y value (in radians).
     * @param x The x value (in radians).
     * @return The arc tangent of the x and y value.
     */
    public static float atan2(float y, float x) {
        return (float) StrictMath.atan2(y, x);
    }

    /**
     * Get the absolute value of a float value. If the argument is not negative, the argument is returned. If the
     * argument is negative, the negation of the argument is returned
     *
     * @param value The value.
     * @return The absolute value of the argument.
     */
    public static float abs(float value) {
        return StrictMath.abs(value);
    }

    /**
     * Calculates the distance between two points.
     *
     * @param x1 The X coordinate of point 1.
     * @param y1 The Y coordinate of point 1.
     * @param x2 The X coordinate of point 2.
     * @param y2 The Y coordinate of point 2.
     * @return The distance between the two given points.
     */
    public static float distance(float x1, float y1, float x2, float y2) {
        return MathHelper.sqrt(squaredDistance(x1, y1, x2, y2));
    }

    /**
     * Calculates the squared distance between two points.
     *
     * @param x1 The X coordinate of point 1.
     * @param y1 The Y coordinate of point 1.
     * @param x2 The X coordinate of point 2.
     * @param y2 The Y coordinate of point 2.
     * @return The squared distance between the two given points.
     */
    public static float squaredDistance(float x1, float y1, float x2, float y2) {
        float vx = x1 - x2;
        float vy = y1 - y2;
        return vx * vx + vy * vy;
    }

    /**
     * Generates a random true or false value.
     *
     * @return True or false.
     */
    public static boolean random() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    /**
     * Generates a random integer number between two numbers.
     *
     * @param min The minimum value (inclusive).
     * @param max The maximum value (exclusive).
     * @return The random integer number.
     */
    public static int random(int min, int max) {
        return ThreadLocalRandom.current().nextInt(max - min) + min;
    }

    /**
     * Generates a random float number between two numbers.
     *
     * @param min The minimum value (inclusive).
     * @param max The maximum value (exclusive).
     * @return The random float number.
     */
    public static float random(float min, float max) {
        return min + ThreadLocalRandom.current().nextFloat() * (max - min);
    }

    /**
     * Determines the greater value of the given values.
     *
     * @param a The first value.
     * @param b The second value.
     * @return The greater value.
     */
    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    /**
     * Determines the smaller value of the given values.
     *
     * @param a The first value.
     * @param b The second value.
     * @return The smaller value.
     */
    public static float min(float a, float b) {
        return Math.min(a, b);
    }

    /**
     * Determines the greater value of the given values.
     *
     * @param values The values.
     * @return The greater value.
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
     * Determines the smaller value of the given values.
     *
     * @param values The values.
     * @return The smaller value.
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
     * Clamps the given value between the given min and max values.
     *
     * @param value The value to clamp.
     * @param min   The minimum value.
     * @param max   The maximum value.
     * @return The clamped value.
     */
    public static long clamp(long value, long min, long max) {
        return value < min ? min : value > max ? max : value;
    }

    /**
     * Clamps the given value between the given min and max values.
     *
     * @param value The value to clamp.
     * @param min   The minimum value.
     * @param max   The maximum value.
     * @return The clamped value.
     */
    public static float clamp(float value, float min, float max) {
        return value < min ? min : value > max ? max : value;
    }

    /**
     * Clamps the given value between the given min and max values.
     *
     * @param value The value to clamp.
     * @param min   The minimum value.
     * @param max   The maximum value.
     * @return The clamped value.
     */
    public static int clamp(int value, int min, int max) {
        return value < min ? min : value > max ? max : value;
    }

    /**
     * Converts the given degrees to radians.
     *
     * @param degrees The degrees to convert.
     * @return The radians.
     */
    public static float toRadians(float degrees) {
        return degrees * 0.0174532925f;
    }

    /**
     * Converts the given radians to degrees.
     *
     * @param radians The radians to convert.
     * @return The degrees.
     */
    public static float toDegrees(float radians) {
        return radians * 57.295779513f;
    }

    /**
     * Linear interpolation between two points.
     *
     * @param start    The first point.
     * @param end      The second point.
     * @param position The interpolation position value [0-1].
     * @return The interpolated value.
     */
    public static byte linearInterpolation(byte start, byte end, float position) {
        return (byte) (start + (end - start) * position);
    }

    /**
     * Linear interpolation between two points.
     *
     * @param start    The first point.
     * @param end      The second point.
     * @param position The interpolation position value [0-1].
     * @return The interpolated value.
     */
    public static float linearInterpolation(float start, float end, float position) {
        return start + (end - start) * position;
    }

    /**
     * Linear interpolation between two points.
     *
     * @param start    The first point.
     * @param end      The second point.
     * @param position The interpolation position value [0-1].
     * @return The interpolated value.
     */
    public static int linearInterpolation(int start, int end, int position) {
        return start + (end - start) * position;
    }

    /**
     * Rotates a point around a given origin.
     *
     * @param point  The point to rotate.
     * @param origin The origin.
     * @param angle  The angle to rotate in radians.
     * @return The rotated point.
     */
    public static Vector2 rotatePoint(Vector2 point, Vector2 origin, float angle) {
        return rotatePoint(point.getX(), point.getY(), origin.getX(), origin.getY(), angle);
    }

    /**
     * Rotates a point around a given origin.
     *
     * @param x       The x-coordinate of the point to rotate.
     * @param y       The y-coordinate of the point to rotate.
     * @param originX The x-coordinate of the origin.
     * @param originY The y-coordinate of the origin.
     * @param angle   The angle to rotate in radians.
     * @return The rotated point.
     */
    public static Vector2 rotatePoint(float x, float y, float originX, float originY, float angle) {
        return new Vector2(originX + (x - originX) * cos(angle) - (y - originY) * sin(angle),
                originY + (y - originY) * cos(angle) + (x - originX) * sin(angle));
    }

    /**
     * Calculates the resulting direction for a given target based on the current origin.
     *
     * @param origin The current origin.
     * @param target The target.
     * @return The resulting direction.
     */
    public static float direction(Vector2 origin, Vector2 target) {
        return direction(origin.getX(), origin.getY(), target.getX(), target.getY());
    }

    /**
     * Calculates the resulting direction for a given target based on the current origin.
     *
     * @param originX The current x-coordinate of the origin.
     * @param originY The current y-coordinate of the origin.
     * @param targetX The target x-coordinate.
     * @param targetY The target y-coordinate.
     * @return The resulting direction.
     */
    public static float direction(float originX, float originY, float targetX, float targetY) {
        return (float) Math.atan2(targetY - originY, targetX - originX);
    }

    /**
     * Checks for polygon collision (vertices are required to be ordered either clockwise or counter-clockwise). This
     * approach is based on the SAT (Separating Axis Theorem).
     *
     * @param polygonVerticesA The vertices of the first polygon.
     * @param polygonVerticesB The vertices of the second polygon.
     * @return True if the polygons collide, false otherwise.
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
     * @param line    The probe line.
     * @param polygon The polygon.
     * @return The intersection point(s) if any.
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
     * @param a The first line.
     * @param b The second line.
     * @return The intersection point if any.
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
     * @param p1x1 The x-coordinate of the first line's first point.
     * @param p1y1 The y-coordinate of the first line's first point.
     * @param p1x2 The x-coordinate of the first line's second point.
     * @param p1y2 The y-coordinate of the first line's second point.
     * @param p2x1 The x-coordinate of the second line's first point.
     * @param p2y1 The y-coordinate of the second line's first point.
     * @param p2x2 The x-coordinate of the second line's second point.
     * @param p2y2 The y-coordinate of the second line's second point.
     * @return The intersection point if any.
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
