/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.io.Serializable;

public class Vector3 implements Serializable {

    //region static

    public static final Vector3 ZERO = Vector3.zero();
    public static final Vector3 ONE = Vector3.one();
    public static final Vector3 HALF = Vector3.half();

    //endregion

    //region properties

    private float x;
    private float y;
    private float z;

    //endregion

    //region constructors

    /**
     * Constructor.
     */
    public Vector3() {
        this.setX(0);
        this.setY(0);
        this.setZ(0);
    }

    /**
     * Constructor.
     *
     * @param xyz The x, y and z coordinates of the vector.
     */
    public Vector3(float xyz) {
        this.setX(xyz);
        this.setY(xyz);
        this.setZ(xyz);
    }

    /**
     * Constructor.
     *
     * @param x The x coordinate of the vector.
     * @param y The y coordinate of the vector.
     * @param z The z coordinate of the vector.
     */
    public Vector3(float x, float y, float z) {
        this.setX(x);
        this.setY(y);
        this.setZ(z);
    }

    /**
     * Constructor.
     *
     * @param other The vector to copy.
     */
    public Vector3(Vector3 other) {
        if (other != null) {
            this.setX(other.getX());
            this.setY(other.getY());
            this.setZ(other.getZ());

        } else {
            this.set(0);
        }
    }

    //endregion

    //region public methods

    /**
     * Adds the vector to this vector.
     *
     * @param vec3 The vector to add.
     */
    public void add(Vector3 vec3) {
        this.setX(this.getX() + vec3.getX());
        this.setY(this.getY() + vec3.getY());
        this.setZ(this.getZ() + vec3.getZ());
    }

    /**
     * Adds the given coordinates to the vector.
     *
     * @param x The x coordinate of the vector.
     * @param y The y coordinate of the vector.
     * @param z The z coordinate of the vector.
     */
    public void add(float x, float y, float z) {
        this.setX(this.getX() + x);
        this.setY(this.getY() + y);
        this.setZ(this.getZ() + z);
    }

    /**
     * Subtracts the vector from this vector.
     *
     * @param vec3 The vector to subtract.
     */
    public void subtract(Vector3 vec3) {
        this.setX(this.getX() - vec3.getX());
        this.setY(this.getY() - vec3.getY());
        this.setZ(this.getZ() - vec3.getZ());
    }

    /**
     * Sets the vector to the given coordinates.
     *
     * @param x The x coordinate of the vector.
     * @param y The y coordinate of the vector.
     * @param z The z coordinate of the vector.
     */
    public void subtract(float x, float y, float z) {
        this.setX(this.getX() - x);
        this.setY(this.getY() - y);
        this.setZ(this.getZ() - z);
    }

    /**
     * Multiplies the vector by the given vector.
     *
     * @param vec3 The vector to multiply.
     */
    public void multiply(Vector3 vec3) {
        this.setX(this.getX() * vec3.getX());
        this.setY(this.getY() * vec3.getY());
        this.setZ(this.getZ() * vec3.getZ());
    }

    /**
     * Multiplies the vector by the given coordinates.
     *
     * @param x The x coordinate of the vector.
     * @param y The y coordinate of the vector.
     * @param z The z coordinate of the vector.
     */
    public void multiply(float x, float y, float z) {
        this.setX(this.getX() * x);
        this.setY(this.getY() * y);
        this.setZ(this.getZ() * z);
    }

    /**
     * Normalizes the vector (length of 1).
     */
    public void normalize() {
        float value = 1.0f / this.length();
        this.setX(this.getX() * value);
        this.setY(this.getY() * value);
        this.setZ(this.getZ() * value);
    }

    /**
     * Calculates the distance from vector to target.
     *
     * @param target The target vector.
     * @return The distance.
     */
    public float distanceTo(Vector3 target) {
        return (float) Math.sqrt(Vector3.squaredDistance(this, target));
    }

    /**
     * The dot product of this vector with another vector.
     *
     * @param vec The other vector.
     * @return The dot product.
     */
    public float dot(Vector3 vec) {
        return this.getX() * vec.getX() + this.getY() * vec.getY() + this.getZ() * vec.getZ();
    }

    /**
     * Calculates the squared lenght of the vector.
     *
     * @return The squared length.
     */
    public float lengthSquared() {
        return getX() * getX() + getY() * getY() + getZ() * getZ();
    }

    /**
     * Set vector values.
     *
     * @param x The x coordinate of the vector.
     * @param y The y coordinate of the vector.
     * @param z The z coordinate of the vector.
     */
    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Set vector values.
     *
     * @param xyz The xyz coordinates of the vector.
     */
    public void set(float xyz) {
        this.x = xyz;
        this.y = xyz;
        this.z = xyz;
    }

    /**
     * Set vector values (x, y).
     *
     * @param o The vector to copy.
     */
    public void set(Vector2 o) {
        this.x = o.getX();
        this.y = o.getY();
    }

    /**
     * Set vector values.
     *
     * @param o The vector to copy.
     */
    public void set(Vector3 o) {
        this.x = o.x;
        this.y = o.y;
        this.z = o.z;
    }

    /**
     * Calculates the length of the vector.
     *
     * @return The length of the vector.
     */
    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    /**
     * Get the x coordinate of the vector.
     *
     * @return The x coordinate of the vector.
     */
    public float getX() {
        return x;
    }

    /**
     * Set the x coordinate of the vector.
     *
     * @param x The x coordinate of the vector.
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Get the y coordinate of the vector.
     *
     * @return The y coordinate of the vector.
     */
    public float getY() {
        return y;
    }

    /**
     * Set the y coordinate of the vector.
     *
     * @param y The y coordinate of the vector.
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * Get the z coordinate of the vector.
     *
     * @return The z coordinate of the vector.
     */
    public float getZ() {
        return z;
    }

    /**
     * Set the z coordinate of the vector.
     *
     * @param z The z coordinate of the vector.
     */
    public void setZ(float z) {
        this.z = z;
    }

    /**
     * Creates a new instance of the vector with values (0, 0, 0).
     *
     * @return The new vector.
     */
    public static Vector3 zero() {
        return new Vector3(0);
    }

    /**
     * Creates a new instance of the vector with values (1, 1, 1).
     *
     * @return The new vector.
     */
    public static Vector3 one() {
        return new Vector3(1);
    }

    /**
     * Creates a new instance of the vector with values (0.5, 0.5, 0.5).
     *
     * @return The new vector.
     */
    public static Vector3 half() {
        return new Vector3(0.5f);
    }

    @Override
    public String toString() {
        return String.format("%s: [x: %f, y: %f, z: %f]", this.getClass().getSimpleName(), getX(), getY(), getZ());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vector3) {
            return ((Vector3) obj).getX() == this.getX()
                    && ((Vector3) obj).getY() == this.getY()
                    && ((Vector3) obj).getZ() == this.getZ();
        }
        return false;
    }

    //endregion

    //region public static methods

    /**
     * Normalizes the vector and returns the result as a new instance.
     *
     * @param vec The vector to normalize.
     * @return The normalized vector.
     */
    public static Vector3 normalize(Vector3 vec) {
        float value = 1.0f / vec.length();
        return new Vector3(vec.getX() * value, vec.getY() * value, vec.getZ() * value);
    }

    /**
     * Calculates the distance between two vectors.
     *
     * @param vec1 The first vector.
     * @param vec2 The second vector.
     * @return The distance between the two vectors.
     */
    public static float distance(Vector3 vec1, Vector3 vec2) {
        return (float) Math.sqrt(Vector3.squaredDistance(vec1, vec2));
    }

    /**
     * Calculates the squared distance between two vectors.
     *
     * @param vec1 The first vector.
     * @param vec2 The second vector.
     * @return The squared distance between the two vectors.
     */
    public static float squaredDistance(Vector3 vec1, Vector3 vec2) {
        float vx = vec1.getX() - vec2.getX();
        float vy = vec1.getY() - vec2.getY();
        float vz = vec1.getZ() - vec2.getZ();
        return vx * vx + vy * vy + vz * vz;
    }

    //endregion
}
