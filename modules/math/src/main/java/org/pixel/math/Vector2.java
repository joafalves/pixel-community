/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import lombok.Builder;

import java.io.Serializable;
import java.util.Objects;

public class Vector2 implements Serializable {

    //region static

    public static final Vector2 ZERO = zero();
    public static final Vector2 ONE = one();
    public static final Vector2 ZERO_ONE = zeroOne();
    public static final Vector2 ONE_ZERO = oneZero();
    public static final Vector2 HALF = half();
    public static final Vector2 UP = zeroOne();
    public static final Vector2 RIGHT = oneZero();
    public static final Vector2 LEFT = new Vector2(-1, 0);
    public static final Vector2 DOWN = new Vector2(0, -1);

    //endregion

    //region properties

    private float x;
    private float y;

    //endregion

    //region constructors

    /**
     * Constructor.
     */
    public Vector2() {
        this.set(0);
    }

    /**
     * Constructor.
     *
     * @param xy The xy values of the vector.
     */
    public Vector2(float xy) {
        this.setX(xy);
        this.setY(xy);
    }

    /**
     * Constructor.
     *
     * @param x The x value of the vector.
     * @param y The y value of the vector.
     */
    @Builder
    public Vector2(float x, float y) {
        this.setX(x);
        this.setY(y);
    }

    /**
     * Constructor.
     *
     * @param other The other vector.
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
     * Add the given vector values to this vector.
     *
     * @param vec2 The vector to add.
     */
    public void add(Vector2 vec2) {
        this.setX(this.getX() + vec2.getX());
        this.setY(this.getY() + vec2.getY());
    }

    /**
     * Add the given coordinates to this vector.
     *
     * @param x The x value to add.
     * @param y The y value to add.
     */
    public void add(float x, float y) {
        this.setX(this.getX() + x);
        this.setY(this.getY() + y);
    }

    /**
     * Add the given coordinates to this vector.
     *
     * @param xy The xy values to add.
     */
    public void add(float xy) {
        this.setX(this.getX() + xy);
        this.setY(this.getY() + xy);
    }

    /**
     * Subtract the given vector values from this vector.
     *
     * @param vec2 The vector to subtract.
     */
    public void subtract(Vector2 vec2) {
        this.setX(this.getX() - vec2.getX());
        this.setY(this.getY() - vec2.getY());
    }

    /**
     * Subtract the given coordinates from this vector.
     *
     * @param x The x value to subtract.
     * @param y The y value to subtract.
     */
    public void subtract(float x, float y) {
        this.setX(this.getX() - x);
        this.setY(this.getY() - y);
    }


    /**
     * Subtract the given coordinates from this vector.
     *
     * @param xy The xy values to subtract.
     */
    public void subtract(float xy) {
        this.setX(this.getX() - xy);
        this.setY(this.getY() - xy);
    }

    /**
     * Multiply this vector by the given vector.
     *
     * @param vec2 The vector to multiply by.
     */
    public void multiply(Vector2 vec2) {
        this.setX(this.getX() * vec2.getX());
        this.setY(this.getY() * vec2.getY());
    }

    /**
     * Multiply this vector by the given coordinates.
     *
     * @param x The x value to multiply by.
     * @param y The y value to multiply by.
     */
    public void multiply(float x, float y) {
        this.x *= x;
        this.y *= y;
    }

    /**
     * Multiply this vector by the given coordinates.
     *
     * @param xy The xy values to multiply by.
     */
    public void multiply(float xy) {
        this.x *= xy;
        this.y *= xy;
    }

    /**
     * Divide this vector by the given vector.
     *
     * @param vec2 The vector to divide by.
     */
    public void divide(Vector2 vec2) {
        this.setX(this.getX() / vec2.getX());
        this.setY(this.getY() / vec2.getY());
    }

    /**
     * Divide this vector by the given coordinates.
     *
     * @param x The x value to divide by.
     * @param y The y value to divide by.
     */
    public void divide(float x, float y) {
        this.x /= x;
        this.y /= y;
    }

    /**
     * Divide this vector by the given coordinates.
     *
     * @param xy The xy values to divide by.
     */
    public void divide(float xy) {
        this.x /= xy;
        this.y /= xy;
    }

    /**
     * Move coordinates towards the target point by the given amount.
     *
     * @param target The target position vector.
     * @param amount The amount to move towards the target position (range from 0 to 1).
     */
    public void moveTo(Vector2 target, float amount) {
        float distance = distanceTo(target);
        float directionX = (target.getX() - this.getX()) / distance;
        float directionY = (target.getY() - this.getY()) / distance;
        this.setX(this.getX() + directionX * amount);
        this.setY(this.getY() + directionY * amount);
    }

    /**
     * Normalizes the vector (length of 1).
     */
    public void normalize() {
        if (this.length() == 0) {
            throw new RuntimeException("The length of the vector is 0");
        }
        float value = 1.f / this.length();
        this.setX(this.getX() * value);
        this.setY(this.getY() * value);
    }

    /**
     * Rotates the vector by the given angle.
     *
     * @param origin The center of rotation.
     * @param angle  The angle to rotate by (in radians).
     */
    public void rotateAround(Vector2 origin, float angle) {
        this.set(origin.getX() + (x - origin.getX()) * MathHelper.cos(angle)
                        - (y - origin.getY()) * MathHelper.sin(angle),
                origin.getY() + (y - origin.getY()) * MathHelper.cos(angle)
                        + (x - origin.getX()) * MathHelper.sin(angle));
    }

    /**
     * Set the values.
     *
     * @param o The vector to copy.
     */
    public void set(Vector2 o) {
        this.x = o.x;
        this.y = o.y;
    }

    /**
     * Set vector values (x, y).
     *
     * @param o The vector to copy.
     */
    public void set(Vector3 o) {
        this.x = o.getX();
        this.y = o.getY();
    }

    /**
     * Set vector values.
     *
     * @param xy The xy values to set.
     */
    public void set(float xy) {
        this.x = xy;
        this.y = xy;
    }

    /**
     * Set vector values.
     *
     * @param x The x value to set.
     * @param y The y value to set.
     */
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Transforms the vector by the given matrix.
     *
     * @param mat The matrix to transform by.
     */
    public void transformMatrix4(Matrix4 mat) {
        float[][] matrixArray = mat.toUnsafeArray();
        float ox = x;
        float oy = y;
        x = matrixArray[0][0] * ox + matrixArray[1][0] * oy + matrixArray[3][0];
        y = matrixArray[0][1] * ox + matrixArray[1][1] * oy + matrixArray[3][1];
    }

    /**
     * Clamp the vector to the given minimum and maximum values.
     *
     * @param minX The minimum x value.
     * @param maxX The maximum x value.
     * @param minY The minimum y value.
     * @param maxY The maximum y value.
     */
    public void clamp(float minX, float maxX, float minY, float maxY) {
        x = MathHelper.clamp(x, minX, maxX);
        y = MathHelper.clamp(y, minY, maxY);
    }

    /**
     * Calculates the distance to target.
     *
     * @param target The target position vector.
     * @return The distance to target.
     */
    public float distanceTo(Vector2 target) {
        return (float) Math.sqrt(Vector2.squaredDistance(this, target));
    }

    /**
     * The dot product of this vector with another vector.
     *
     * @param vec The vector to dot with.
     * @return The dot product.
     */
    public float dot(Vector2 vec) {
        return this.getX() * vec.getX() + this.getY() * vec.getY();
    }

    /**
     * Calculates the left normal of this vector.
     *
     * @return The left normal.
     */
    public Vector2 leftNormal() {
        return new Vector2(this.getY(), -1 * this.getX());
    }

    /**
     * Calculates the right normal of this vector.
     *
     * @return The right normal.
     */
    public Vector2 rightNormal() {
        return new Vector2(-1 * this.getY(), this.getX());
    }

    /**
     * Calculates the squared length of this vector.
     *
     * @return The length of this vector.
     */
    public float lengthSquared() {
        return x * x + y * y;
    }

    /**
     * Calculates the length of this vector.
     *
     * @return The length of this vector.
     */
    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    /**
     * Get the coordinate x value.
     *
     * @return The x value.
     */
    public float getX() {
        return x;
    }

    /**
     * Set the coordinate x value.
     *
     * @param x The x value.
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Get the coordinate y value.
     *
     * @return The y value.
     */
    public float getY() {
        return y;
    }

    /**
     * Set the coordinate y value.
     *
     * @param y The y value.
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * Creates a new instance of the vector with values (0, 0).
     *
     * @return The new vector.
     */
    public static Vector2 zero() {
        return new Vector2(0);
    }

    /**
     * Creates a new instance of the vector with values (0, 1).
     *
     * @return The new vector.
     */
    public static Vector2 zeroOne() {
        return new Vector2(0, 1);
    }

    /**
     * Creates a new instance of the vector with values (1, 1).
     *
     * @return The new vector.
     */
    public static Vector2 one() {
        return new Vector2(1);
    }

    /**
     * Creates a new instance of the vector with values (1, 0).
     *
     * @return The new vector.
     */
    public static Vector2 oneZero() {
        return new Vector2(1, 0);
    }

    /**
     * Creates a new instance of the vector with values (0.5, 0.5).
     *
     * @return The new vector.
     */
    public static Vector2 half() {
        return new Vector2(0.5f, 0.5f);
    }

    @Override
    public String toString() {
        return String.format("%s: [x: %f, y: %f]", this.getClass().getSimpleName(), getX(), getY());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector2)) return false;
        Vector2 vector2 = (Vector2) o;
        return vector2.getX() == this.getX() && vector2.getY() == this.getY();
    }
    //endregion

    //region public static methods

    /**
     * Calculates the cross product of a vector with given value.
     *
     * @param vec   The vector.
     * @param value The value.
     * @return The cross product.
     */
    public static Vector2 cross(Vector2 vec, float value) {
        return new Vector2(vec.x * -value, vec.y * value);
    }

    /**
     * Calculates the cross product of two vectors.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return The cross product.
     */
    public static float cross(Vector2 a, Vector2 b) {
        return a.x * b.y - a.y * b.x;
    }

    /**
     * Adds two vectors and returns the result as a new instance.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return The result of the addition.
     */
    public static Vector2 add(Vector2 a, Vector2 b) {
        Vector2 vec = new Vector2(a);
        vec.add(b);
        return vec;
    }

    /**
     * Subtracts two vectors and returns the result as a new instance.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return The result of the subtraction.
     */
    public static Vector2 subtract(Vector2 a, Vector2 b) {
        Vector2 vec = new Vector2(a);
        vec.subtract(b);
        return vec;
    }

    /**
     * Multiplies two vectors and returns the result as a new instance.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return The result of the multiplication.
     */
    public static Vector2 multiply(Vector2 a, Vector2 b) {
        Vector2 vec = new Vector2(a);
        vec.multiply(b);
        return vec;
    }

    /**
     * Calculates the dot product of two vectors.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return The dot product.
     */
    public static float dot(Vector2 a, Vector2 b) {
        return dot(a.x, a.y, b.x, b.y);
    }

    /**
     * Calculates the dot product of two vectors.
     *
     * @param ax The x value of the first vector.
     * @param ay The y value of the first vector.
     * @param bx The x value of the second vector.
     * @param by The y value of the second vector.
     * @return The dot product.
     */
    public static float dot(float ax, float ay, float bx, float by) {
        return ax * bx + ay * by;
    }

    /**
     * Divides two vectors and returns the result as a new instance.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return The result of the division.
     */
    public static Vector2 divide(Vector2 a, Vector2 b) {
        Vector2 vec = new Vector2(a);
        vec.divide(b);
        return vec;
    }

    /**
     * Normalizes the vector and returns the result as a new instance.
     *
     * @param vec The vector.
     * @return The normalized vector.
     */
    public static Vector2 normalize(Vector2 vec) {
        float value = 1.0f / vec.length();
        return new Vector2(vec.getX() * value, vec.getY() * value);
    }

    /**
     * Calculates the distance between two vectors.
     *
     * @param vec1 The first vector.
     * @param vec2 The second vector.
     * @return The distance.
     */
    public static float distance(Vector2 vec1, Vector2 vec2) {
        return (float) Math.sqrt(Vector2.squaredDistance(vec1, vec2));
    }

    /**
     * Calculates the squared distance between two vectors.
     *
     * @param vec1 The first vector.
     * @param vec2 The second vector.
     * @return The squared distance.
     */
    public static float squaredDistance(Vector2 vec1, Vector2 vec2) {
        float vx = vec1.getX() - vec2.getX();
        float vy = vec1.getY() - vec2.getY();
        return vx * vx + vy * vy;
    }

    /**
     * Transforms the vector by the given matrix and returns the result as a new instance.
     *
     * @param vec The vector.
     * @param mat The matrix.
     * @return The transformed vector.
     */
    public static Vector2 transformMatrix4(Vector2 vec, Matrix4 mat) {
        return transformMatrix4(vec.x, vec.y, mat);
    }

    /**
     * Transforms the vector by the given matrix and returns the result as a new instance.
     *
     * @param x   The x value of the vector.
     * @param y   The y value of the vector.
     * @param mat The matrix.
     * @return The transformed vector.
     */
    public static Vector2 transformMatrix4(float x, float y, Matrix4 mat) {
        float[][] ua = mat.toUnsafeArray();
        return new Vector2(ua[0][0] * x + ua[1][0] * y + ua[3][0], ua[0][1] * x + ua[1][1] * y + ua[3][1]);
    }

    //endregion

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
