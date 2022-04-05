/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import lombok.Builder;

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
     * Constructor.
     *
     * @param topLeft     The top left coordinate.
     * @param topRight    The top right coordinate.
     * @param bottomLeft  The bottom left coordinate.
     * @param bottomRight The bottom right coordinate.
     */
    @Builder
    public Boundary(Vector2 topLeft, Vector2 topRight, Vector2 bottomLeft, Vector2 bottomRight) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
        this.vectorCache = null;
    }

    /**
     * Constructor.
     *
     * @param topLeftX     The top left x coordinate.
     * @param topLeftY     The top left y coordinate.
     * @param topRightX    The top right x coordinate.
     * @param topRightY    The top right y coordinate.
     * @param bottomLeftX  The bottom left x coordinate.
     * @param bottomLeftY  The bottom left y coordinate.
     * @param bottomRightX The bottom right x coordinate.
     * @param bottomRightY The bottom right y coordinate.
     */
    public Boundary(float topLeftX, float topLeftY, float topRightX, float topRightY, float bottomLeftX,
            float bottomLeftY, float bottomRightX, float bottomRightY) {
        this.topLeft = new Vector2(topLeftX, topLeftY);
        this.topRight = new Vector2(topRightX, topRightY);
        this.bottomLeft = new Vector2(bottomLeftX, bottomLeftY);
        this.bottomRight = new Vector2(bottomRightX, bottomRightY);
        this.vectorCache = null;
    }

    /**
     * Constructor.
     *
     * @param position The position of the center of the boundary.
     * @param bulk     The bulk of the boundary.
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
     * Constructor.
     *
     * @param x      The top left x coordinate.
     * @param y      The top left y coordinate.
     * @param width  The width of the boundary.
     * @param height The height of the boundary.
     */
    public Boundary(float x, float y, float width, float height) {
        this.topLeft = new Vector2(x, y);
        this.topRight = new Vector2(x + width, y);
        this.bottomLeft = new Vector2(x, y + height);
        this.bottomRight = new Vector2(x + width, y + height);
        this.vectorCache = null;
    }

    /**
     * Constructor.
     *
     * @param other The other boundary.
     */
    public Boundary(Boundary other) {
        this.topLeft = new Vector2(other.topLeft);
        this.topRight = new Vector2(other.topRight);
        this.bottomLeft = new Vector2(other.bottomLeft);
        this.bottomRight = new Vector2(other.bottomRight);
        this.vectorCache = null;
    }

    /**
     * Constructor.
     *
     * @param rectangle The rectangle to extract the boundary from.
     */
    public Boundary(Rectangle rectangle) {
        this.topLeft = new Vector2(rectangle.getX(), rectangle.getY());
        this.topRight = new Vector2(rectangle.getX() + rectangle.getWidth(), rectangle.getY());
        this.bottomLeft = new Vector2(rectangle.getX(), rectangle.getY() + rectangle.getHeight());
        this.bottomRight = new Vector2(rectangle.getX() + rectangle.getWidth(),
                rectangle.getY() + rectangle.getHeight());
        this.vectorCache = null;
    }

    //endregion

    //region public methods

    /**
     * Test if current boundary is overlapping another.
     *
     * @param other The other boundary.
     * @return True if overlapping, false otherwise.
     */
    public boolean overlaps(Boundary other) {
        return overlap(this, other);
    }

    /**
     * Returns all vertices in an array (clock-wise).
     *
     * @return {[{topLeft: Vector2, topRight:Vector2, bottomRight: Vector2, bottomLeft: Vector2}]} The vertices.
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
     * Update boundary values.
     *
     * @param x      The top left x coordinate.
     * @param y      The top left y coordinate.
     * @param width  The width of the boundary.
     * @param height The height of the boundary.
     */
    public void set(float x, float y, float width, float height) {
        this.topLeft.set(x, y);
        this.topRight.set(x + width, y);
        this.bottomLeft.set(x, y + height);
        this.bottomRight.set(x + width, y + height);
    }

    /**
     * Set Top Left coordinate.
     *
     * @param topLeft The top left coordinate.
     */
    public void setTopLeft(Vector2 topLeft) {
        this.topLeft.set(topLeft);
    }

    /**
     * Set Top Right coordinate.
     *
     * @param topRight The top right coordinate.
     */
    public void setTopRight(Vector2 topRight) {
        this.topRight.set(topRight);
    }

    /**
     * Set Bottom Left coordinate.
     *
     * @param bottomLeft The bottom left coordinate.
     */
    public void setBottomLeft(Vector2 bottomLeft) {
        this.bottomLeft.set(bottomLeft);
    }

    /**
     * Set Bottom Right coordinate.
     *
     * @param bottomRight The bottom right coordinate.
     */
    public void setBottomRight(Vector2 bottomRight) {
        this.bottomRight.set(bottomRight);
    }

    /**
     * Get the top left coordinate.
     *
     * @return The top left coordinate.
     */
    public Vector2 getTopLeft() {
        return this.topLeft;
    }

    /**
     * Get the top right coordinate.
     *
     * @return The top right coordinate.
     */
    public Vector2 getTopRight() {
        return this.topRight;
    }

    /**
     * Get the bottom left coordinate.
     *
     * @return The bottom left coordinate.
     */
    public Vector2 getBottomLeft() {
        return this.bottomLeft;
    }

    /**
     * Get the bottom right coordinate.
     *
     * @return The bottom right coordinate.
     */
    public Vector2 getBottomRight() {
        return this.bottomRight;
    }

    /**
     * Get the most left X coordinate of this boundary.
     *
     * @return The most left X coordinate.
     */
    public float getLeft() {
        return this.bottomLeft.getX() < this.topLeft.getX() ? this.bottomLeft.getX() : this.topLeft.getX();
    }

    /**
     * Get the most top Y coordinate of this boundary.
     *
     * @return The most top Y coordinate.
     */
    public float getTop() {
        return this.topRight.getY() < this.topLeft.getY() ? this.topRight.getY() : this.topLeft.getY();
    }

    /**
     * Get the most right X coordinate of this boundary.
     *
     * @return The most right X coordinate.
     */
    public float getRight() {
        return this.topRight.getX() > this.bottomRight.getX() ? this.topRight.getX() : this.bottomRight.getX();
    }

    /**
     * Get the most top Y coordinate of this boundary.
     *
     * @return The most top Y coordinate.
     */
    public float getBottom() {
        return this.bottomRight.getY() > this.bottomLeft.getY() ? this.bottomRight.getY() : this.bottomLeft.getY();
    }

    /**
     * Get boundary width.
     *
     * @return The boundary width.
     */
    public float getWidth() {
        return this.getRight() - this.getLeft();
    }

    /**
     * Get boundary height.
     *
     * @return The boundary height.
     */
    public float getHeight() {
        return this.getBottom() - this.getTop();
    }

    /**
     * Rotate all the vertices of this boundary around the given origin.
     *
     * @param origin The origin of the rotation.
     * @param angle  The angle of the rotation.
     */
    public void rotate(Vector2 origin, float angle) {
        this.topLeft.rotateAround(origin, angle);
        this.topRight.rotateAround(origin, angle);
        this.bottomLeft.rotateAround(origin, angle);
        this.bottomRight.rotateAround(origin, angle);
    }

    //endregion

    //region public static methods

    /**
     * Tests if two boundaries are overlapping each other
     *
     * @param a The first boundary.
     * @param b The second boundary.
     * @return True if the boundaries are overlapping each other.
     */
    public static boolean overlap(Boundary a, Boundary b) {
        return MathHelper.overlap(a.getVertices(), b.getVertices());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Boundary) {
            return ((Boundary) obj).topLeft.equals(this.topLeft) && ((Boundary) obj).topRight.equals(this.topRight)
                    && ((Boundary) obj).bottomLeft.equals(this.bottomLeft) && ((Boundary) obj).bottomRight.equals(
                    this.bottomRight);
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
