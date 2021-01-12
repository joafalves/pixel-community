/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.io.Serializable;

public class Vector2 implements Serializable {

    //region properties

    private float x;
    private float y;

    //endregion

    //region constructors

    /**
     * Constructor
     */
    public Vector2() {
        this.set(0);
    }

    /**
     * Constructor
     *
     * @param xy
     */
    public Vector2(float xy) {
        this.setX(xy);
        this.setY(xy);
    }

    /**
     * @param x
     * @param y
     */
    public Vector2(float x, float y) {
        this.setX(x);
        this.setY(y);
    }

    /**
     * @param other
     */
    public Vector2(Vector2 other) {
        if (other != null) {
            this.setX(other.getX());
            this.setY(other.getY());

        } else {
            this.set(0);
        }
    }

    //endregion

    //region public methods

    /**
     * @param vec2
     */
    public void add(Vector2 vec2) {
        this.setX(this.getX() + vec2.getX());
        this.setY(this.getY() + vec2.getY());
    }

    /**
     * @param x
     * @param y
     */
    public void add(float x, float y) {
        this.setX(this.getX() + x);
        this.setY(this.getY() + y);
    }

    /**
     * @param vec2
     */
    public void subtract(Vector2 vec2) {
        this.setX(this.getX() - vec2.getX());
        this.setY(this.getY() - vec2.getY());
    }

    /**
     * @param x
     * @param y
     */
    public void subtract(float x, float y) {
        this.setX(this.getX() - x);
        this.setY(this.getY() - y);
    }

    /**
     * @param vec2
     */
    public void multiply(Vector2 vec2) {
        this.setX(this.getX() * vec2.getX());
        this.setY(this.getY() * vec2.getY());
    }

    /**
     * @param x
     * @param y
     */
    public void multiply(float x, float y) {
        this.x *= x;
        this.y *= y;
    }

    /**
     * @param vec2
     */
    public void divide(Vector2 vec2) {
        this.setX(this.getX() / vec2.getX());
        this.setY(this.getY() / vec2.getY());
    }

    /**
     * @param x
     * @param y
     */
    public void divide(float x, float y) {
        this.x /= x;
        this.y /= y;
    }

    /**
     * Normalizes the vector2
     */
    public void normalize() {
        if (this.length() == 0) return;
        float value = 1.f / this.length();
        this.setX(this.getX() * value);
        this.setY(this.getY() * value);
    }

    /**
     * Set vector values
     *
     * @param o
     */
    public void set(Vector2 o) {
        this.x = o.x;
        this.y = o.y;
    }

    /**
     * Set vector values
     *
     * @param o
     */
    public void set(Vector3 o) {
        this.x = o.getX();
        this.y = o.getY();
    }

    /**
     * Set vector values
     *
     * @param xy
     */
    public void set(float xy) {
        this.x = xy;
        this.y = xy;
    }

    /**
     * Set vector values
     *
     * @param x
     * @param y
     */
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @param mat
     */
    public void transformMatrix4(Matrix4 mat) {
        float[][] ua = mat.toUnsafeArray();
        float ox = x;
        float oy = y;
        x = ua[0][0] * ox + ua[1][0] * oy + ua[3][0];
        y = ua[0][1] * ox + ua[1][1] * oy + ua[3][1];
    }

    /**
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     */
    public void clamp(float minX, float maxX, float minY, float maxY) {
        x = MathHelper.clamp(x, minX, maxX);
        y = MathHelper.clamp(y, minY, maxY);
    }

    /**
     * Calculates the distance from vector to target
     *
     * @param target
     * @return
     */
    public float distanceTo(Vector2 target) {
        return (float) Math.sqrt(Vector2.squaredDistance(this, target));
    }

    /**
     * The dot product of this vector with another vector
     *
     * @param vec
     * @return
     */
    public float dot(Vector2 vec) {
        return this.getX() * vec.getX() + this.getY() * vec.getY();
    }

    /**
     * Calculates the left normal of this vector
     *
     * @return
     */
    public Vector2 leftNormal() {
        return new Vector2(this.getY(), -1 * this.getX());
    }

    /**
     * Calculates the right normal of this vector
     *
     * @return
     */
    public Vector2 rightNormal() {
        return new Vector2(-1 * this.getY(), this.getX());
    }

    /**
     * @return
     */
    public float lengthSquared() {
        return x * x + y * y;
    }

    /**
     * @return
     */
    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    /**
     * Get vector X value
     *
     * @return
     */
    public float getX() {
        return x;
    }

    /**
     * Set vector X value
     *
     * @param x
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Get vector Y value
     *
     * @return
     */
    public float getY() {
        return y;
    }

    /**
     * Set vector Y value
     *
     * @param y
     */
    public void setY(float y) {
        this.y = y;
    }

    public static Vector2 zero() {
        return new Vector2(0);
    }

    public static Vector2 zeroOne() {
        return new Vector2(0, 1);
    }

    public static Vector2 one() {
        return new Vector2(1);
    }

    public static Vector2 oneZero() {
        return new Vector2(1, 0);
    }

    public static Vector2 half() {
        return new Vector2(0.5f, 0.5f);
    }

    @Override
    public String toString() {
        return String.format("%s: [x: %f, y: %f]", Vector2.class.getName(), getX(), getY());
    }

    //endregion

    //region public static methods

    /**
     * @param vec
     * @param value
     * @return
     */
    public static Vector2 cross(Vector2 vec, float value) {
        return new Vector2(vec.x * -value, vec.y * value);
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static float cross(Vector2 a, Vector2 b) {
        return a.x * b.y - a.y * b.x;
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static Vector2 add(Vector2 a, Vector2 b) {
        Vector2 vec = new Vector2(a);
        vec.add(b);
        return vec;
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static Vector2 subtract(Vector2 a, Vector2 b) {
        Vector2 vec = new Vector2(a);
        vec.subtract(b);
        return vec;
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static Vector2 multiply(Vector2 a, Vector2 b) {
        Vector2 vec = new Vector2(a);
        vec.multiply(b);
        return vec;
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static float dot(Vector2 a, Vector2 b) {
        return a.x * b.x + a.y * b.y;
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static Vector2 divide(Vector2 a, Vector2 b) {
        Vector2 vec = new Vector2(a);
        vec.divide(b);
        return vec;
    }

    /**
     * Calculates a normalized vector without affecting the original
     *
     * @param vec
     * @return
     */
    public static Vector2 normalize(Vector2 vec) {
        float value = 1.0f / vec.length();
        return new Vector2(vec.getX() * value, vec.getY() * value);
    }

    /**
     * Calculates the distance between two vectors
     *
     * @param vec1
     * @param vec2
     * @return
     */
    public static float distance(Vector2 vec1, Vector2 vec2) {
        return (float) Math.sqrt(Vector2.squaredDistance(vec1, vec2));
    }

    /**
     * Calculates the squared distance between two vectors
     *
     * @param vec1
     * @param vec2
     * @return
     */
    public static float squaredDistance(Vector2 vec1, Vector2 vec2) {
        float vx = vec1.getX() - vec2.getX();
        float vy = vec1.getY() - vec2.getY();
        return vx * vx + vy * vy;
    }

    /**
     * @param vec
     * @param mat
     * @return
     */
    public static Vector2 transformMatrix4(Vector2 vec, Matrix4 mat) {
        return transformMatrix4(vec.x, vec.y, mat);
    }

    /**
     * @param x
     * @param y
     * @param mat
     * @return
     */
    public static Vector2 transformMatrix4(float x, float y, Matrix4 mat) {
        float[][] ua = mat.toUnsafeArray();
        return new Vector2(ua[0][0] * x + ua[1][0] * y + ua[3][0], ua[0][1] * x + ua[1][1] * y + ua[3][1]);
    }

    //endregion
}
