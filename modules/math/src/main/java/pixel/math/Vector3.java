/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.math;

import java.io.Serializable;

public class Vector3 implements Serializable {

    //region properties

    private float x;
    private float y;
    private float z;

    //endregion

    //region constructors

    /**
     * Constructor
     */
    public Vector3() {
        this.setX(0);
        this.setY(0);
        this.setZ(0);
    }

    /**
     * Constructor
     *
     * @param xyz
     */
    public Vector3(float xyz) {
        this.setX(xyz);
        this.setY(xyz);
        this.setZ(xyz);
    }

    /**
     * @param x
     * @param y
     * @param z
     */
    public Vector3(float x, float y, float z) {
        this.setX(x);
        this.setY(y);
        this.setZ(z);
    }

    /**
     * @param other
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
     * @param vec3
     */
    public void add(Vector3 vec3) {
        this.setX(this.getX() + vec3.getX());
        this.setY(this.getY() + vec3.getY());
        this.setZ(this.getZ() + vec3.getZ());
    }

    /**
     * @param x
     * @param y
     * @param z
     */
    public void add(float x, float y, float z) {
        this.setX(this.getX() + x);
        this.setY(this.getY() + y);
        this.setZ(this.getZ() + z);
    }

    /**
     * @param vec3
     */
    public void subtract(Vector3 vec3) {
        this.setX(this.getX() - vec3.getX());
        this.setY(this.getY() - vec3.getY());
        this.setZ(this.getZ() - vec3.getZ());
    }

    /**
     * @param x
     * @param y
     * @param z
     */
    public void subtract(float x, float y, float z) {
        this.setX(this.getX() - x);
        this.setY(this.getY() - y);
        this.setZ(this.getZ() - z);
    }

    /**
     * @param vec3
     */
    public void multiply(Vector3 vec3) {
        this.setX(this.getX() * vec3.getX());
        this.setY(this.getY() * vec3.getY());
        this.setZ(this.getZ() * vec3.getZ());
    }

    /**
     * @param x
     * @param y
     * @param z
     */
    public void multiply(float x, float y, float z) {
        this.setX(this.getX() * x);
        this.setY(this.getY() * y);
        this.setZ(this.getZ() * z);
    }

    /**
     * Normalizes the vector2
     */
    public void normalize() {
        float value = 1.0f / this.length();
        this.setX(this.getX() * value);
        this.setY(this.getY() * value);
        this.setZ(this.getZ() * value);
    }

    /**
     * Calculates the distance from vector to target
     *
     * @param target
     * @return
     */
    public float distanceTo(Vector3 target) {
        return (float) Math.sqrt(Vector3.squaredDistance(this, target));
    }

    /**
     * The dot product of this vector with another vector
     *
     * @param vec
     * @return
     */
    public float dot(Vector3 vec) {
        return this.getX() * vec.getX() + this.getY() * vec.getY() + this.getZ() * vec.getZ();
    }

    /**
     * @return
     */
    public float lengthSquared() {
        return getX() * getX() + getY() * getY() + getZ() * getZ();
    }

    /**
     * Set vecto values
     *
     * @param x
     * @param y
     * @param z
     */
    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Set vector values
     *
     * @param xyz
     */
    public void set(float xyz) {
        this.x = xyz;
        this.y = xyz;
        this.z = xyz;
    }

    /**
     * Set vector values
     *
     * @param o
     */
    public void set(Vector2 o) {
        this.x = o.getX();
        this.y = o.getY();
    }

    /**
     * Set vector values
     *
     * @param o
     */
    public void set(Vector3 o) {
        this.x = o.x;
        this.y = o.y;
        this.z = o.z;
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

    /**
     * Get vector Z value
     *
     * @return
     */
    public float getZ() {
        return z;
    }

    /**
     * Set vector Z value
     *
     * @param z
     */
    public void setZ(float z) {
        this.z = z;
    }

    public static Vector3 zero() {
        return new Vector3(0);
    }

    public static Vector3 one() {
        return new Vector3(0);
    }

    @Override
    public String toString() {
        return String.format("%s: [x: %f, y: %f, z: %f]", Vector3.class.getName(), getX(), getY(), getZ());
    }

    //endregion

    //region public static methods

    /**
     * Calculates a normalized vector without affecting the original
     *
     * @param vec
     * @return
     */
    public static Vector3 normalize(Vector3 vec) {
        float value = 1.0f / vec.length();
        return new Vector3(vec.getX() * value, vec.getY() * value, vec.getZ() * value);
    }

    /**
     * Calculates the distance between two vectors
     *
     * @param vec1
     * @param vec2
     * @return
     */
    public static float distance(Vector3 vec1, Vector3 vec2) {
        return (float) Math.sqrt(Vector3.squaredDistance(vec1, vec2));
    }

    /**
     * Calculates the squared distance between two vectors
     *
     * @param vec1
     * @param vec2
     * @return
     */
    public static float squaredDistance(Vector3 vec1, Vector3 vec2) {
        float vx = vec1.getX() - vec2.getX();
        float vy = vec1.getY() - vec2.getY();
        float vz = vec1.getZ() - vec2.getZ();
        return vx * vx + vy * vy + vz * vz;
    }

    //endregion
}
