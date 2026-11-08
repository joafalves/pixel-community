/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.core;

import org.pixel.math.Matrix4;
import org.pixel.math.Vector2;

import java.io.Serializable;

public class Camera2D implements Camera, Serializable {

    //region private properties

    private final Vector2 position;
    private final Vector2 origin;
    private float width;
    private float height;
    private float zoom;
    private Matrix4 matrixCache;
    private boolean dirty;
    private transient GameWindow window; // display density, for getPixelsPerUnit()

    //endregion

    //region constructors

    /**
     * Constructor. Sets the camera size to the game's virtual size.
     *
     * @param context The game context.
     */
    public Camera2D(GameContext context) {
        this(0, 0, context.settings().getVirtualWidth(), context.settings().getVirtualHeight());
        this.window = context.window();
    }

    /**
     * Constructor. Sets the camera size to the game's virtual size.
     *
     * @param context The game context.
     * @param origin  The camera view origin.
     */
    public Camera2D(GameContext context, Vector2 origin) {
        this(0, 0, context.settings().getVirtualWidth(), context.settings().getVirtualHeight(), 1.0f, origin);
        this.window = context.window();
    }

    /**
     * Constructor.
     *
     * @param width  The viewport width of the camera.
     * @param height The viewport height of the camera.
     */
    public Camera2D(float width, float height) {
        this(0, 0, width, height);
    }

    /**
     * Constructor.
     *
     * @param x      The x position of the camera.
     * @param y      The y position of the camera.
     * @param width  The viewport width of the camera.
     * @param height The viewport height of the camera.
     */
    public Camera2D(float x, float y, float width, float height) {
        this(x, y, width, height, 1.0f);
    }

    /**
     * Constructor.
     *
     * @param x      The x position of the camera.
     * @param y      The y position of the camera.
     * @param width  The viewport width of the camera.
     * @param height The viewport height of the camera.
     * @param zoom   The zoom factor of the camera.
     */
    public Camera2D(float x, float y, float width, float height, float zoom) {
        this.position = new Vector2(x, y);
        this.width = width;
        this.height = height;
        this.zoom = zoom;
        this.dirty = true;
        this.origin = new Vector2(0.f);
    }

    /**
     * Constructor.
     *
     * @param x      The x position of the camera.
     * @param y      The y position of the camera.
     * @param width  The viewport width of the camera.
     * @param height The viewport height of the camera.
     * @param zoom   The zoom factor of the camera.
     * @param origin The camera view origin.
     */
    public Camera2D(float x, float y, float width, float height, float zoom, Vector2 origin) {
        this.position = new Vector2(x, y);
        this.width = width;
        this.height = height;
        this.zoom = zoom;
        this.dirty = true;
        this.origin = new Vector2(origin); // copy: origin is mutated by setOrigin()
    }

    //endregion

    //region private methods

    /**
     * Always calculates a view matrix regardless of the existing cache.
     */
    private void computeMatrix() {
        if (matrixCache == null) {
            matrixCache = new Matrix4();
        }

        float left = position.getX() - getWidth() / zoom * getOrigin().getX();
        float top = position.getY() - getHeight() / zoom * getOrigin().getY();
        matrixCache.setOrthographic(left, left + getWidth() / zoom, top + getHeight() / zoom, top, 0.0f, 1.0f);
    }

    //endregion

    //region public methods

    /**
     * Get the view matrix. The view matrix is calculated only when the camera properties are modified.
     *
     * @return The view matrix.
     */
    @Override
    public Matrix4 getViewMatrix() {
        if (matrixCache == null || dirty) {
            computeMatrix();
            dirty = false; // matrix updated, clear dirty flag
        }

        return matrixCache;
    }

    /**
     * Transforms a given screen position to virtual coordinates.
     *
     * @param screenPosition The screen position.
     * @return The virtual position.
     */
    public Vector2 screenToVirtualCoordinates(Vector2 screenPosition) {
        return screenToVirtualCoordinates(screenPosition.getX(), screenPosition.getY());
    }

    /**
     * Transforms a given screen position to virtual coordinates.
     * <p>
     * The screen position must be expressed in the camera's viewport space (0..width, 0..height), e.g. the
     * mouse position from {@code Mouse.getVirtualPosition()} — not raw window coordinates, which differ when the
     * window is resized or scaled by the OS.
     *
     * @param screenX The screen x position.
     * @param screenY The screen y position.
     * @return The virtual position.
     */
    public Vector2 screenToVirtualCoordinates(float screenX, float screenY) {
        // normalize screen position:
        float nx = 2.0f * screenX / getWidth() - 1.0f;
        float ny = 1.0f - 2.0f * screenY / getHeight();
        Matrix4 invertMatrix = new Matrix4(getViewMatrix());
        invertMatrix.invert();

        return Vector2.transformMatrix4(new Vector2(nx, ny), invertMatrix);
    }

    /**
     * Get the position of the camera.
     *
     * @return The position of the camera.
     */
    public Vector2 getPosition() {
        return this.position;
    }

    /**
     * Get the position (x-axis) of the camera.
     *
     * @return The position (x-axis) of the camera.
     */
    public float getPositionX() {
        return this.position.getX();
    }

    /**
     * Get the position (y-axis) of the camera.
     *
     * @return The position (y-axis) of the camera.
     */
    public float getPositionY() {
        return this.position.getY();
    }

    /**
     * Translate the camera by the given amount.
     *
     * @param x The amount to translate the camera on the x-axis.
     * @param y The amount to translate the camera on the y-axis.
     */
    public void translate(float x, float y) {
        this.position.add(x, y);
        this.dirty = true;
    }

    /**
     * Set camera position to the given values.
     *
     * @param x The new x-position of the camera.
     * @param y The new y-position of the camera.
     */
    public void setPosition(float x, float y) {
        this.position.set(x, y);
        this.dirty = true;
    }

    /**
     * Set camera position to the given values.
     *
     * @param position The new position of the camera.
     */
    public void setPosition(Vector2 position) {
        this.position.set(position.getX(), position.getY());
        this.dirty = true;
    }

    /**
     * Set camera origin view to the given values.
     *
     * @param xy The new origin of the camera (both axis).
     */
    public void setOrigin(float xy) {
        this.origin.set(xy, xy);
        this.dirty = true;
    }

    /**
     * Set camera origin to the given values.
     *
     * @param x The new x-origin of the camera.
     * @param y The new y-origin of the camera.
     */
    public void setOrigin(float x, float y) {
        this.origin.set(x, y);
        this.dirty = true;
    }

    /**
     * Set camera origin to the given values.
     *
     * @param origin The new origin of the camera.
     */
    public void setOrigin(Vector2 origin) {
        this.origin.set(origin.getX(), origin.getY());
        this.dirty = true;
    }

    /**
     * Get camera origin.
     *
     * @return The camera origin.
     */
    public Vector2 getOrigin() {
        return origin;
    }

    /**
     * Set camera zoom to the given value.
     *
     * @param zoom The new zoom value.
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
        this.dirty = true;
    }

    /**
     * Framebuffer pixels per world unit: the window's framebuffer size over the world area the camera shows
     * ({@code size / zoom}), on the tighter axis. Cameras created without a game context don't know the window and
     * assume one pixel per unit at zoom 1.
     */
    @Override
    public float getPixelsPerUnit() {
        if (window == null || width <= 0 || height <= 0) {
            return zoom;
        }
        return Math.min(window.getWidth() * zoom / width, window.getHeight() * zoom / height);
    }

    /**
     * Get camera zoom value.
     *
     * @return The camera zoom value.
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Set camera width to the given value.
     *
     * @param width The new width value.
     */
    public void setWidth(float width) {
        this.width = width;
        this.dirty = true;
    }

    /**
     * Get camera width value.
     *
     * @return The camera width value.
     */
    public float getWidth() {
        return width;
    }

    /**
     * Set camera height to the given value.
     *
     * @param height The new height value.
     */
    public void setHeight(float height) {
        this.height = height;
        this.dirty = true;
    }

    /**
     * Get camera height value.
     *
     * @return The camera height value.
     */
    public float getHeight() {
        return height;
    }

    /**
     * Set camera size to the given values.
     *
     * @param width  The new width value.
     * @param height The new height value.
     */
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        this.dirty = true;
    }

    /**
     * Set dirty state of the camera (force {@link #getViewMatrix()}. to calculate on the next call).
     */
    public void setDirty() {
        this.dirty = true;
    }

    //endregion
}
