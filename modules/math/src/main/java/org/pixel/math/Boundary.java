/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Boundary implements Serializable {

    //region public properties

    private final Vector2 topLeft;
    private final Vector2 topRight;
    private final Vector2 bottomLeft;
    private final Vector2 bottomRight;
    private List<Vector2> vectorCache;

    //endregion

    //region constructors

    /**
     * Constructor
     *
     * @param topLeft
     * @param topRight
     * @param bottomLeft
     * @param bottomRight
     */
    public Boundary(Vector2 topLeft, Vector2 topRight, Vector2 bottomLeft, Vector2 bottomRight) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
        this.vectorCache = null;
    }

    /**
     * Constructor
     *
     * @param topLeftX
     * @param topLeftY
     * @param topRightX
     * @param topRightY
     * @param bottomLeftX
     * @param bottomLeftY
     * @param bottomRightX
     * @param bottomRightY
     */
    public Boundary(float topLeftX, float topLeftY, float topRightX, float topRightY,
            float bottomLeftX, float bottomLeftY, float bottomRightX, float bottomRightY) {
        this.topLeft = new Vector2(topLeftX, topLeftY);
        this.topRight = new Vector2(topRightX, topRightY);
        this.bottomLeft = new Vector2(bottomLeftX, bottomLeftY);
        this.bottomRight = new Vector2(bottomRightX, bottomRightY);
        this.vectorCache = null;
    }

    /**
     * Constructor
     *
     * @param position
     * @param bulk
     */
    public Boundary(Vector2 position, float bulk) {
        float halfBulk = bulk / 2.0f;
        this.topLeft = new Vector2(position.getX() - halfBulk, position.getY() - halfBulk);
        this.topRight = new Vector2(position.getX() + halfBulk, position.getY() - halfBulk);
        this.bottomLeft = new Vector2(position.getX() - halfBulk, position.getY() + halfBulk);
        this.bottomRight = new Vector2(position.getX() + halfBulk, position.getY() + halfBulk);
        this.vectorCache = null;
    }

    /**
     * Constructor
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public Boundary(float x, float y, float width, float height) {
        this.topLeft = new Vector2(x, y);
        this.topRight = new Vector2(x + width, y);
        this.bottomLeft = new Vector2(x, y + height);
        this.bottomRight = new Vector2(x + width, y + height);
        this.vectorCache = null;
    }

    //endregion

    //region public methods

    /**
     * Test if current boundary is overlapping another
     *
     * @param other
     * @return
     */
    public boolean overlapsWith(Boundary other) {
        return overlap(this, other);
    }

    /**
     * Returns all vertices in an array (clock-wise)
     *
     * @return {[{topLeft: Vector2, topRight:Vector2, bottomRight: Vector2, bottomLeft: Vector2}]}
     */
    public List<Vector2> getVertices() {
        if (vectorCache == null) {
            vectorCache = new ArrayList<>(4);
            vectorCache.add(this.topLeft);
            vectorCache.add(this.topRight);
            vectorCache.add(this.bottomRight);
            vectorCache.add(this.bottomLeft);
        }

        return vectorCache;
    }

    /**
     * Update boundary values
     *
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void set(float x, float y, float width, float height) {
        this.topLeft.set(x, y);
        this.topRight.set(x + width, y);
        this.bottomLeft.set(x, y + height);
        this.bottomRight.set(x + width, y + height);
    }

    /**
     * Set Top Left coordinate
     *
     * @param topLeft
     */
    public void setTopLeft(Vector2 topLeft) {
        this.topLeft.set(topLeft);
    }

    /**
     * Set Top Right coordinate
     *
     * @param topRight
     */
    public void setTopRight(Vector2 topRight) {
        this.topRight.set(topRight);
    }

    /**
     * Set Bottom Left coordinate
     *
     * @param bottomLeft
     */
    public void setBottomLeft(Vector2 bottomLeft) {
        this.bottomLeft.set(bottomLeft);
    }

    /**
     * Set Bottom Right coordinate
     *
     * @param bottomRight
     */
    public void setBottomRight(Vector2 bottomRight) {
        this.bottomRight.set(bottomRight);
    }

    /**
     * @return
     */
    public Vector2 getTopLeft() {
        return this.topLeft;
    }

    /**
     * @return
     */
    public Vector2 getTopRight() {
        return this.topRight;
    }

    /**
     * @return
     */
    public Vector2 getBottomLeft() {
        return this.bottomLeft;
    }

    /**
     * @return
     */
    public Vector2 getBottomRight() {
        return this.bottomRight;
    }

    /**
     * Get the most left X coordinate of this boundary
     *
     * @return
     */
    public float getLeft() {
        return this.bottomLeft.getX() < this.topLeft.getX() ? this.bottomLeft.getX() : this.topLeft.getX();
    }

    /**
     * Get the most top Y coordinate of this boundary
     *
     * @return
     */
    public float getTop() {
        return this.topRight.getY() < this.topLeft.getY() ? this.topRight.getY() : this.topLeft.getY();
    }

    /**
     * Get the most right X coordinate of this boundary
     *
     * @return
     */
    public float getRight() {
        return this.topRight.getX() > this.bottomRight.getX() ? this.topRight.getX() : this.bottomRight.getX();
    }

    /**
     * Get the most top Y coordinate of this boundary
     *
     * @return
     */
    public float getBottom() {
        return this.bottomRight.getY() > this.bottomLeft.getY() ? this.bottomRight.getY() : this.bottomLeft.getY();
    }

    /**
     * Get boundary width
     *
     * @return
     */
    public float getWidth() {
        return this.getRight() - this.getLeft();
    }

    /**
     * Get boundary height
     *
     * @return
     */
    public float getHeight() {
        return this.getBottom() - this.getTop();
    }

    //endregion

    //region public static methods

    /**
     * Tests if two boundaries are overlapping each other
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean overlap(Boundary a, Boundary b) {
        return MathHelper.overlap(a.getVertices(), b.getVertices());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Boundary) {
            return ((Boundary) obj).topLeft.equals(this.topLeft)
                    && ((Boundary) obj).topRight.equals(this.topRight)
                    && ((Boundary) obj).bottomLeft.equals(this.bottomLeft)
                    && ((Boundary) obj).bottomRight.equals(this.bottomRight);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s: [topLeft: '%s', topRight: '%s', bottomLeft: '%s', bottomRight: '%s']",
                this.getClass().getSimpleName(), this.topLeft, this.topRight, this.bottomLeft, this.bottomRight);
    }

    //endregion
}
