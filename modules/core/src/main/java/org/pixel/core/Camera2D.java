/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.math.Matrix4;
import org.pixel.math.Vector2;

import java.io.Serializable;

public class Camera2D implements Disposable, Serializable {

    //region private properties

    private float width;
    private float height;
    private float zoom;
    private Vector2 position;
    private Vector2 origin;
    private Matrix4 matrixCache;
    private boolean dirty;

    //endregion

    //region constructors

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public Camera2D(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.width = width;
        this.height = height;
        this.dirty = true;
        this.zoom = 1.0f;
        this.origin = new Vector2(0.5f);
        this.matrixCache = new Matrix4();
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @param zoom
     */
    public Camera2D(float x, float y, float width, float height, float zoom) {
        this.position = new Vector2(x, y);
        this.width = width;
        this.height = height;
        this.zoom = zoom;
        this.dirty = true;
        this.origin = new Vector2(0.5f);
    }

    //endregion

    //region private methods

    /**
     * Always calculates a view matrix (no caching)
     *
     * @return
     */
    private void computeMatrix() {
        float left = position.getX() - getWidth() / zoom * getOrigin().getX();
        float top = position.getY() - getHeight() / zoom * getOrigin().getY();
        matrixCache.setOrthographic(left, left + getWidth() / zoom, top + getHeight() / zoom, top, 0.0f, 1.0f);
    }

    //endregion

    //region public methods

    /**
     * Calculates the view matrix when needed (cache based)
     *
     * @return
     */
    public Matrix4 getViewMatrix() {
        if (matrixCache == null || this.dirty) {
            computeMatrix();
            dirty = false; // matrix updated, clear dirty flag
        }

        return matrixCache;
    }

    /**
     * Transforms a given screen position to game coordinates
     *
     * @param screenPosition
     * @return
     */
    public Vector2 screenToGameCoordinates(Vector2 screenPosition) {
        return screenToGameCoordinates(screenPosition.getX(), screenPosition.getY());
    }

    /**
     * Transforms a given screen position to game coordinates
     *
     * @return
     */
    public Vector2 screenToGameCoordinates(float screenX, float screenY) {
        // normalize screen position:
        float nx = 2.0f * screenX / getWidth() - 1.0f;
        float ny = 1.0f - 2.0f * screenY / getHeight();
        Matrix4 invertMatrix = getViewMatrix().clone();
        invertMatrix.invert();

        return Vector2.transformMatrix4(new Vector2(nx, ny), invertMatrix);
    }

    public Vector2 getPosition() {
        return this.position;
    }

    /**
     * @return
     */
    public float getPositionX() {
        return this.position.getX();
    }

    /**
     * @return
     */
    public float getPositionY() {
        return this.position.getY();
    }

    /**
     * Add camera position
     *
     * @param x
     * @param y
     */
    public void translate(float x, float y) {
        this.position.add(x, y);
        this.dirty = true;
    }

    /**
     * Set camera position
     *
     * @param x
     * @param y
     */
    public void setPosition(float x, float y) {
        this.position.set(x, y);
        this.dirty = true;
    }

    /**
     * Set camera position
     *
     * @param position
     */
    public void setPosition(Vector2 position) {
        this.position.set(position.getX(), position.getY());
        this.dirty = true;
    }

    /**
     * Set camera origin
     *
     * @param xy
     */
    public void setOrigin(float xy) {
        this.origin.set(xy, xy);
        this.dirty = true;
    }

    /**
     * Set camera origin
     *
     * @param x
     * @param y
     */
    public void setOrigin(float x, float y) {
        this.origin.set(x, y);
        this.dirty = true;
    }

    /**
     * Set camera origin
     *
     * @param origin
     */
    public void setOrigin(Vector2 origin) {
        this.origin.set(origin.getX(), origin.getY());
        this.dirty = true;
    }

    /**
     * Get camera origin
     *
     * @return
     */
    public Vector2 getOrigin() {
        return origin;
    }

    /**
     * Set camera zoom
     *
     * @param zoom
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
        this.dirty = true;
    }

    /**
     * Get camera zoom
     *
     * @return
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Set camera width
     *
     * @param width
     */
    public void setWidth(float width) {
        this.width = width;
        this.dirty = true;
    }

    /**
     * Get camera width
     *
     * @return
     */
    public float getWidth() {
        return width;
    }

    /**
     * Set camera height
     *
     * @param height
     */
    public void setHeight(float height) {
        this.height = height;
        this.dirty = true;
    }

    /**
     * Get camera height
     *
     * @return
     */
    public float getHeight() {
        return height;
    }

    /**
     * Set camera size
     *
     * @param width
     * @param height
     */
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        this.dirty = true;
    }

    /**
     * Set dirty state
     */
    public void setDirty() {
        this.dirty = true;
    }

    /**
     * Dispose this object
     */
    @Override
    public void dispose() {

    }

    //endregion
}
