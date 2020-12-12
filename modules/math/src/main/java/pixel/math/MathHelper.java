/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.math;

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
    public static float degToRad(float degree) {
        return degree * 0.0174532925f;
    }

    /**
     * @param radian
     * @return
     */
    public static float radToDeg(float radian) {
        return radian * 57.295779513f;
    }

    /**
     * Checks for polygon collision (vertices are required to be ordered either clockwise or counter-clockwise)
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

    //endregion
}
