/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Builder;

public class Rectangle implements Serializable {
    //region Fields & Properties

    private float x;
    private float y;
    private float width;
    private float height;
    private List<Vector2> vectorCache;

    //endregion

    //region Constructors

    /**
     * Constructor.
     */
    public Rectangle() {
        this.x = 0;
        this.y = 0;
        this.width = 1;
        this.height = 1;
        this.vectorCache = null;
    }

    /**
     * Constructor.
     *
     * @param xywh The x, y, width and height of the rectangle.
     */
    public Rectangle(float xywh) {
        this.x = xywh;
        this.y = xywh;
        this.width = xywh;
        this.height = xywh;
        this.vectorCache = null;
    }

    /**
     * Constructor.
     *
     * @param x      The x coordinate of the rectangle.
     * @param y      The y coordinate of the rectangle.
     * @param width  The width of the rectangle.
     * @param height The height of the rectangle.
     */
    @Builder
    public Rectangle(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.vectorCache = null;
    }

    /**
     * Constructor.
     *
     * @param position The position of the rectangle.
     * @param bulk     The bulk of the rectangle.
     */
    public Rectangle(Vector2 position, float bulk) {
        float halfBulk = bulk / 2.0f;
        this.x = position.getX() - halfBulk;
        this.y = position.getY() - halfBulk;
        this.width = bulk;
        this.height = bulk;
        this.vectorCache = null;
    }

    /**
     * Constructor.
     *
     * @param other The other rectangle.
     */
    public Rectangle(Rectangle other) {
        if (other != null) {
            this.x = other.x;
            this.y = other.y;
            this.width = other.width;
            this.height = other.height;

        } else {
            this.set(0, 0, 0, 0);
        }

        this.vectorCache = null;
    }

    //endregion

    //region Public Functions

    /**
     * Returns all vertices in an array (clock-wise).
     *
     * @return {[{topLeft: Vector2, topRight:Vector2, bottomRight: Vector2, bottomLeft: Vector2}]} The vertices.
     */
    public List<Vector2> getVertices() {
        if (vectorCache == null) {
            vectorCache = new ArrayList<>(4);
            vectorCache.add(new Vector2(x, y));
            vectorCache.add(new Vector2(x + width, y));
            vectorCache.add(new Vector2(x + width, y + height));
            vectorCache.add(new Vector2(x, y + height));

        } else {
            // update cache info:
            vectorCache.get(0).set(x, y);
            vectorCache.get(0).set(x + width, y);
            vectorCache.get(0).set(x + width, y + height);
            vectorCache.get(0).set(x, y + height);
        }

        return vectorCache;
    }

    /**
     * Add another rectangle values.
     *
     * @param rectangle The other rectangle.
     */
    public void add(Rectangle rectangle) {
        this.x += rectangle.x;
        this.y += rectangle.y;
        this.width += rectangle.width;
        this.height += rectangle.height;
    }

    /**
     * Subtract another rectangle values
     *
     * @param rectangle The other rectangle.
     */
    public void subtract(Rectangle rectangle) {
        this.x -= rectangle.x;
        this.y -= rectangle.y;
        this.width -= rectangle.width;
        this.height -= rectangle.height;
    }

    /**
     * Expands the rectangle by the given amount.
     *
     * @param amount The amount to expand.
     */
    public void expand(float amount) {
        this.x -= amount;
        this.y -= amount;
        this.width += amount * 2;
        this.height += amount * 2;
    }

    /**
     * Expands the rectangle by the given amount.
     *
     * @param amountX The amount to expand on the x-axis.
     * @param amountY The amount to expand on the y-axis.
     */
    public void expand(float amountX, float amountY) {
        this.x -= amountX;
        this.y -= amountY;
        this.width += amountX * 2;
        this.height += amountY * 2;
    }

    /**
     * Shrinks the rectangle by the given amount.
     *
     * @param amount The amount to shrink.
     */
    public void shrink(float amount) {
        this.x += amount;
        this.y += amount;
        this.width -= amount * 2;
        this.height -= amount * 2;
    }

    /**
     * Shrinks the rectangle by the given amount.
     *
     * @param amountX The amount to shrink on the x-axis.
     * @param amountY The amount to shrink on the y-axis.
     */
    public void shrink(float amountX, float amountY) {
        this.x += amountX;
        this.y += amountY;
        this.width -= amountX * 2;
        this.height -= amountY * 2;
    }

    /**
     * Changes the rectangle properties to the union of the current rectangle and the given rectangle.
     *
     * @param other The other rectangle.
     */
    public void union(Rectangle other) {
        float x1 = Math.min(x, other.x);
        float x2 = Math.max(x + width, other.x + other.width);
        float y1 = Math.min(y, other.y);
        float y2 = Math.max(y + height, other.y + other.height);
        this.setX(x1);
        this.setY(y1);
        this.setWidth(x2 - x1);
        this.setHeight(y2 - y1);
    }

    /**
     * Changes the rectangle properties to the intersection of the current rectangle and the given rectangle.
     *
     * @param other The other rectangle.
     */
    public void intersection(Rectangle other) {
        intersection(other.x, other.y, other.width, other.height);
    }

    /**
     * Changes the rectangle properties to the intersection of the current rectangle and the given rectangle.
     *
     * @param x      The x coordinate of the rectangle.
     * @param y      The y coordinate of the rectangle.
     * @param width  The width of the rectangle.
     * @param height The height of the rectangle.
     */
    public void intersection(float x, float y, float width, float height) {
        float tx1 = this.x;
        float ty1 = this.y;
        float tx2 = tx1;
        tx2 += this.width;
        float ty2 = ty1;
        ty2 += this.height;
        float rx2 = x;
        rx2 += width;
        float ry2 = y;
        ry2 += height;
        if (tx1 < x) {
            tx1 = x;
        }
        if (ty1 < y) {
            ty1 = y;
        }
        if (tx2 > rx2) {
            tx2 = rx2;
        }
        if (ty2 > ry2) {
            ty2 = ry2;
        }
        tx2 -= tx1;
        ty2 -= ty1;
        if (tx2 < Integer.MIN_VALUE) {
            tx2 = Integer.MIN_VALUE;
        }
        if (ty2 < Integer.MIN_VALUE) {
            ty2 = Integer.MIN_VALUE;
        }

        this.set(tx1, ty1, tx2, ty2);
    }

    /**
     * Checks whether the given point is inside the rectangle.
     *
     * @param point The point to check.
     * @return True if the point is inside the rectangle, false otherwise.
     */
    public boolean contains(Vector2 point) {
        return contains(point.getX(), point.getY());
    }

    /**
     * Checks whether the given point is inside the rectangle.
     *
     * @param x The x coordinate of the point.
     * @param y The y coordinate of the point.
     * @return True if the point is inside the rectangle, false otherwise.
     */
    public boolean contains(float x, float y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }

    /**
     * Checks if the rectangle overlap with another.
     *
     * @param other The other rectangle.
     * @return True if the rectangles overlaps, false otherwise.
     */
    public boolean overlaps(Rectangle other) {
        return overlaps(x, y, width, height, other.x, other.y, other.width, other.height);
    }

    /**
     * Checks if this rectangle overlap the given rectangle.
     *
     * @param x      The x coordinate of the rectangle.
     * @param y      The y coordinate of the rectangle.
     * @param width  The width of the rectangle.
     * @param height The height of the rectangle.
     * @return True if the rectangles overlaps, false otherwise.
     */
    public boolean overlaps(float x, float y, float width, float height) {
        return overlaps(this.x, this.y, this.width, this.height, x, y, width, height);
    }

    /**
     * Checks if the given rectangles overlap with each other.
     *
     * @param a The first rectangle.
     * @param b The second rectangle.
     * @return True if the rectangles overlaps, false otherwise.
     */
    public static boolean overlaps(Rectangle a, Rectangle b) {
        return overlaps(a.x, a.y, a.width, a.height, b.x, b.y, b.width, b.height);
    }

    /**
     * Checks if the given rectangles overlap with each other.
     *
     * @param ax The x coordinate of the first rectangle.
     * @param ay The y coordinate of the first rectangle.
     * @param aw The width of the first rectangle.
     * @param ah The height of the first rectangle.
     * @param bx The x coordinate of the second rectangle.
     * @param by The y coordinate of the second rectangle.
     * @param bw The width of the second rectangle.
     * @param bh The height of the second rectangle.
     * @return True if the rectangles overlaps, false otherwise.
     */
    public static boolean overlaps(float ax, float ay, float aw, float ah, float bx, float by, float bw, float bh) {
        float tw = aw;
        float th = ah;
        float rw = bw;
        float rh = bh;
        if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
            return false;
        }

        rw += bx;
        rh += by;
        tw += ax;
        th += ay;

        // overflow || intersect
        return ((rw < bx || rw > ax) &&
                (rh < by || rh > ay) &&
                (tw < ax || tw > bx) &&
                (th < ay || th > by));
    }

    /**
     * Get the width of the rectangle.
     *
     * @return The width of the rectangle.
     */
    public float getWidth() {
        return width;
    }

    /**
     * Set the width of the rectangle.
     *
     * @param width The width of the rectangle.
     */
    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * Get the height of the rectangle.
     *
     * @return The height of the rectangle.
     */
    public float getHeight() {
        return height;
    }

    /**
     * Set the height of the rectangle.
     *
     * @param height The height of the rectangle.
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * Get the y coordinate of the rectangle.
     *
     * @return The y coordinate of the rectangle.
     */
    public float getY() {
        return y;
    }

    /**
     * Set the y coordinate of the rectangle.
     *
     * @param y The y coordinate of the rectangle.
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * Get the x coordinate of the rectangle.
     *
     * @return The x coordinate of the rectangle.
     */
    public float getX() {
        return x;
    }

    /**
     * Set the x coordinate of the rectangle.
     *
     * @param x The x coordinate of the rectangle.
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * Set the position and size of the rectangle.
     *
     * @param x      The x coordinate of the rectangle.
     * @param y      The y coordinate of the rectangle.
     * @param width  The width of the rectangle.
     * @param height The height of the rectangle.
     */
    public void set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Set the position and size of the rectangle based on another rectangle.
     *
     * @param other The other rectangle.
     */
    public void set(Rectangle other) {
        if (other != null) {
            this.x = other.x;
            this.y = other.y;
            this.width = other.width;
            this.height = other.height;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rectangle)) return false;
        Rectangle rectangle = (Rectangle) o;
        return Float.compare(rectangle.x, x) == 0 && Float.compare(rectangle.y, y) == 0 && Float.compare(rectangle.width, width) == 0 && Float.compare(rectangle.height, height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }

    public String toString() {
        return String.format("%s: [x: %f, y: %f, w: %f, h: %f]",
                this.getClass().getSimpleName(), getX(), getY(), getWidth(), getHeight());
    }

    //endregion
}
