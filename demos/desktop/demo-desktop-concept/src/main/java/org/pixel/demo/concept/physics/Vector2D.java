/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.concept.physics;

import lombok.Builder;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector3;

import java.io.Serializable;
import java.util.Objects;

public class Vector2D implements Serializable {

    //region static

    public static final Vector2D ZERO = zero();
    public static final Vector2D ONE = one();
    public static final Vector2D ZERO_ONE = zeroOne();
    public static final Vector2D ONE_ZERO = oneZero();
    public static final Vector2D HALF = half();
    public static final Vector2D UP = zeroOne();
    public static final Vector2D RIGHT = oneZero();
    public static final Vector2D LEFT = new Vector2D(-1, 0);
    public static final Vector2D DOWN = new Vector2D(0, -1);

    //endregion

    //region properties

    private double x;
    private double y;

    //endregion

    //region constructors

    /**
     * Constructor.
     */
    public Vector2D() {
        this.set(0);
    }

    /**
     * Constructor.
     *
     * @param xy The xy values of the vector.
     */
    public Vector2D(double xy) {
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
    public Vector2D(double x, double y) {
        this.setX(x);
        this.setY(y);
    }

    /**
     * Constructor.
     *
     * @param other The other vector.
     */
    public Vector2D(Vector2D other) {
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
    public void add(Vector2D vec2) {
        this.setX(this.getX() + vec2.getX());
        this.setY(this.getY() + vec2.getY());
    }

    /**
     * Add the given coordinates to this vector.
     *
     * @param x The x value to add.
     * @param y The y value to add.
     */
    public void add(double x, double y) {
        this.setX(this.getX() + x);
        this.setY(this.getY() + y);
    }

    /**
     * Add the given coordinates to this vector.
     *
     * @param xy The xy values to add.
     */
    public void add(double xy) {
        this.setX(this.getX() + xy);
        this.setY(this.getY() + xy);
    }

    /**
     * Subtract the given vector values from this vector.
     *
     * @param vec2 The vector to subtract.
     */
    public void subtract(Vector2D vec2) {
        this.setX(this.getX() - vec2.getX());
        this.setY(this.getY() - vec2.getY());
    }

    /**
     * Subtract the given coordinates from this vector.
     *
     * @param x The x value to subtract.
     * @param y The y value to subtract.
     */
    public void subtract(double x, double y) {
        this.setX(this.getX() - x);
        this.setY(this.getY() - y);
    }


    /**
     * Subtract the given coordinates from this vector.
     *
     * @param xy The xy values to subtract.
     */
    public void subtract(double xy) {
        this.setX(this.getX() - xy);
        this.setY(this.getY() - xy);
    }

    /**
     * Multiply this vector by the given vector.
     *
     * @param vec2 The vector to multiply by.
     */
    public void multiply(Vector2D vec2) {
        this.setX(this.getX() * vec2.getX());
        this.setY(this.getY() * vec2.getY());
    }

    /**
     * Multiply this vector by the given coordinates.
     *
     * @param x The x value to multiply by.
     * @param y The y value to multiply by.
     */
    public void multiply(double x, double y) {
        this.x *= x;
        this.y *= y;
    }

    /**
     * Multiply this vector by the given coordinates.
     *
     * @param xy The xy values to multiply by.
     */
    public void multiply(double xy) {
        this.x *= xy;
        this.y *= xy;
    }

    /**
     * Divide this vector by the given vector.
     *
     * @param vec2 The vector to divide by.
     */
    public void divide(Vector2D vec2) {
        this.setX(this.getX() / vec2.getX());
        this.setY(this.getY() / vec2.getY());
    }

    /**
     * Divide this vector by the given coordinates.
     *
     * @param x The x value to divide by.
     * @param y The y value to divide by.
     */
    public void divide(double x, double y) {
        this.x /= x;
        this.y /= y;
    }

    /**
     * Divide this vector by the given coordinates.
     *
     * @param xy The xy values to divide by.
     */
    public void divide(double xy) {
        this.x /= xy;
        this.y /= xy;
    }

    /**
     * Move coordinates towards the target point by the given amount.
     *
     * @param target The target position vector.
     * @param amount The amount to move towards the target position (range from 0 to 1).
     */
    public void moveTo(Vector2D target, double amount) {
        double distance = distanceTo(target);
        double directionX = (target.getX() - this.getX()) / distance;
        double directionY = (target.getY() - this.getY()) / distance;
        this.setX(this.getX() + directionX * amount);
        this.setY(this.getY() + directionY * amount);
    }

    /**
     * Normalizes the vector (length of 1).
     */
    public void normalize() {
        if (this.length() == 0) {
            return;
        }
        double value = 1.f / this.length();
        this.setX(this.getX() * value);
        this.setY(this.getY() * value);
    }

    /**
     * Rotates the vector by the given angle.
     *
     * @param origin The center of rotation.
     * @param angle  The angle to rotate by (in radians).
     */
    public void rotateAround(Vector2D origin, double angle) {
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
    public void set(Vector2D o) {
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
    public void set(double xy) {
        this.x = xy;
        this.y = xy;
    }

    /**
     * Set vector values.
     *
     * @param x The x value to set.
     * @param y The y value to set.
     */
    public void set(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Clamp the vector to the given minimum and maximum values.
     *
     * @param minX The minimum x value.
     * @param maxX The maximum x value.
     * @param minY The minimum y value.
     * @param maxY The maximum y value.
     */
    public void clamp(double minX, double maxX, double minY, double maxY) {
        x = MathHelper.clamp(x, minX, maxX);
        y = MathHelper.clamp(y, minY, maxY);
    }

    /**
     * Calculates the distance to target.
     *
     * @param target The target position vector.
     * @return The distance to target.
     */
    public double distanceTo(Vector2D target) {
        return (double) Math.sqrt(Vector2D.squaredDistance(this, target));
    }

    /**
     * The dot product of this vector with another vector.
     *
     * @param vec The vector to dot with.
     * @return The dot product.
     */
    public double dot(Vector2D vec) {
        return this.getX() * vec.getX() + this.getY() * vec.getY();
    }

    /**
     * Calculates the left normal of this vector.
     *
     * @return The left normal.
     */
    public Vector2D leftNormal() {
        return new Vector2D(this.getY(), -1 * this.getX());
    }

    /**
     * Calculates the right normal of this vector.
     *
     * @return The right normal.
     */
    public Vector2D rightNormal() {
        return new Vector2D(-1 * this.getY(), this.getX());
    }

    /**
     * Calculates the squared length of this vector.
     *
     * @return The length of this vector.
     */
    public double lengthSquared() {
        return x * x + y * y;
    }

    /**
     * Calculates the length of this vector.
     *
     * @return The length of this vector.
     */
    public double length() {
        return (double) Math.sqrt(lengthSquared());
    }

    /**
     * Get the coordinate x value.
     *
     * @return The x value.
     */
    public double getX() {
        return x;
    }

    /**
     * Set the coordinate x value.
     *
     * @param x The x value.
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Get the coordinate y value.
     *
     * @return The y value.
     */
    public double getY() {
        return y;
    }

    /**
     * Set the coordinate y value.
     *
     * @param y The y value.
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Creates a new instance of the vector with values (0, 0).
     *
     * @return The new vector.
     */
    public static Vector2D zero() {
        return new Vector2D(0);
    }

    /**
     * Creates a new instance of the vector with values (0, 1).
     *
     * @return The new vector.
     */
    public static Vector2D zeroOne() {
        return new Vector2D(0, 1);
    }

    /**
     * Creates a new instance of the vector with values (1, 1).
     *
     * @return The new vector.
     */
    public static Vector2D one() {
        return new Vector2D(1);
    }

    /**
     * Creates a new instance of the vector with values (1, 0).
     *
     * @return The new vector.
     */
    public static Vector2D oneZero() {
        return new Vector2D(1, 0);
    }

    /**
     * Creates a new instance of the vector with values (0.5, 0.5).
     *
     * @return The new vector.
     */
    public static Vector2D half() {
        return new Vector2D(0.5f, 0.5f);
    }

    @Override
    public String toString() {
        return String.format("%s: [x: %f, y: %f]", this.getClass().getSimpleName(), getX(), getY());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector2D vector2)) return false;
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
    public static Vector2D cross(Vector2D vec, double value) {
        return new Vector2D(vec.x * -value, vec.y * value);
    }

    /**do
     * Calculates the cross product of two vectors.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return The cross product.
     */
    public static double cross(Vector2D a, Vector2D b) {
        return a.x * b.y - a.y * b.x;
    }

    /**
     * Adds two vectors and returns the result as a new instance.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return The result of the addition.
     */
    public static Vector2D add(Vector2D a, Vector2D b) {
        Vector2D vec = new Vector2D(a);
        vec.add(b);
        return vec;
    }

    /**
     * Adds the vector by a given value and returns the result as a new instance.
     *
     * @param a The vector.
     * @param b The value.
     * @return The result of the addition.
     */
    public static Vector2D add(Vector2D a, double b) {
        Vector2D vec = new Vector2D(a);
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
    public static Vector2D subtract(Vector2D a, Vector2D b) {
        Vector2D vec = new Vector2D(a);
        vec.subtract(b);
        return vec;
    }

    /**
     * Subtracts the vector by a given value and returns the result as a new instance.
     *
     * @param a The vector.
     * @param b The value.
     * @return The result of the subtraction.
     */
    public static Vector2D subtract(Vector2D a, double b) {
        Vector2D vec = new Vector2D(a);
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
    public static Vector2D multiply(Vector2D a, Vector2D b) {
        Vector2D vec = new Vector2D(a);
        vec.multiply(b);
        return vec;
    }

    /**
     * Multiplies the vector by a given value and returns the result as a new instance.
     *
     * @param a The vector.
     * @param b The value.
     * @return The result of the multiplication.
     */
    public static Vector2D multiply(Vector2D a, double b) {
        Vector2D vec = new Vector2D(a);
        vec.multiply(b);
        return vec;
    }

    /**
     * Divides two vectors and returns the result as a new instance.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return The result of the division.
     */
    public static Vector2D divide(Vector2D a, Vector2D b) {
        Vector2D vec = new Vector2D(a);
        vec.divide(b);
        return vec;
    }

    /**
     * Divides the vector by a given value and returns the result as a new instance.
     *
     * @param a The vector.
     * @param b The value.
     * @return The result of the division.
     */
    public static Vector2D divide(Vector2D a, double b) {
        Vector2D vec = new Vector2D(a);
        vec.divide(b);
        return vec;
    }

    /**
     * Calculates the dot product of two vectors.
     *
     * @param a The first vector.
     * @param b The second vector.
     * @return The dot product.
     */
    public static double dot(Vector2D a, Vector2D b) {
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
    public static double dot(double ax, double ay, double bx, double by) {
        return ax * bx + ay * by;
    }

    /**
     * Normalizes the vector and returns the result as a new instance.
     *
     * @param vec The vector.
     * @return The normalized vector.
     */
    public static Vector2D normalize(Vector2D vec) {
        double value = 1.0f / vec.length();
        return new Vector2D(vec.getX() * value, vec.getY() * value);
    }

    /**
     * Calculates the distance between two vectors.
     *
     * @param vec1 The first vector.
     * @param vec2 The second vector.
     * @return The distance.
     */
    public static double distance(Vector2D vec1, Vector2D vec2) {
        return MathHelper.distance(vec1.getX(), vec1.getY(), vec2.getX(), vec2.getY());
    }

    /**
     * Returns the squared distance between two vectors (no sqrt).
     * @param a First vector.
     * @param b Second vector.
     * @return Squared distance (double).
     */
    public static double squaredDistance(Vector2D a, Vector2D b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return dx * dx + dy * dy;
    }

    //endregion

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
