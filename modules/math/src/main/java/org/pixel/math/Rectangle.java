/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author João Filipe Alves
 */
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
     * Constructor
     */
    public Rectangle() {
        this.x = 0;
        this.y = 0;
        this.width = 1;
        this.height = 1;
        this.vectorCache = null;
    }

    /**
     * Constructor
     *
     * @param xywh
     */
    public Rectangle(float xywh) {
        this.x = xywh;
        this.y = xywh;
        this.width = xywh;
        this.height = xywh;
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
    public Rectangle(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.vectorCache = null;
    }

    /**
     * Constructor
     *
     * @param position
     * @param bulk
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
     * Constructor
     *
     * @param other
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
     * Returns all vertices in an array (clock-wise)
     *
     * @return {[{topLeft: Vector2, topRight:Vector2, bottomRight: Vector2, bottomLeft: Vector2}]}
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
     * Add another rectangle values
     *
     * @param rectangle
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
     * @param rectangle
     */
    public void subtract(Rectangle rectangle) {
        this.x -= rectangle.x;
        this.y -= rectangle.y;
        this.width -= rectangle.width;
        this.height -= rectangle.height;
    }

    /**
     * @param other
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
     * @param other
     */
    public void intersection(Rectangle other) {
        intersection(other.x, other.y, other.width, other.height);
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
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
        if (tx1 < x) tx1 = x;
        if (ty1 < y) ty1 = y;
        if (tx2 > rx2) tx2 = rx2;
        if (ty2 > ry2) ty2 = ry2;
        tx2 -= tx1;
        ty2 -= ty1;
        if (tx2 < Integer.MIN_VALUE) tx2 = Integer.MIN_VALUE;
        if (ty2 < Integer.MIN_VALUE) ty2 = Integer.MIN_VALUE;

        this.set(tx1, ty1, tx2, ty2);
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public boolean intersects(float x, float y, float width, float height) {
        return intersects(this.x, this.y, this.width, this.height, x, y, width, height);
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static boolean intersects(Rectangle a, Rectangle b) {
        return intersects(a.x, a.y, a.width, a.height, b.x, b.y, b.width, b.height);
    }

    /**
     * @param ax
     * @param ay
     * @param aw
     * @param ah
     * @param bx
     * @param by
     * @param bw
     * @param bh
     * @return
     */
    public static boolean intersects(float ax, float ay, float aw, float ah, float bx, float by, float bw, float bh) {
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

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

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

    //endregion
}
