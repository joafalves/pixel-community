package org.pixel.ext.ecs;

import java.io.Serializable;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

public class Transform implements Serializable {

    private GameObject gameObject;

    private final Vector2 position;
    private float scaleX;
    private float scaleY;
    private float rotation;

    private final Vector2 computedPosition;
    private float computedScaleX;
    private float computedScaleY;
    private float computedRotation;

    private boolean dirty;

    /**
     * Constructor.
     */
    public Transform() {
        this(new Vector2());
    }

    /**
     * Constructor
     *
     * @param position The position of the transform.
     */
    public Transform(Vector2 position) {
        this(position, 1, 1, 0);
    }

    /**
     * Constructor.
     *
     * @param position The position of the transform.
     * @param scale    The scale of the transform.
     * @param rotation The rotation of the transform.
     */
    public Transform(Vector2 position, float scale, float rotation) {
        this(position, scale, scale, rotation);
    }

    /**
     * Constructor.
     *
     * @param position The position of the transform.
     * @param scaleX   The scale of the transform on the x-axis.
     * @param scaleY   The scale of the transform on the y-axis.
     * @param rotation The rotation of the transform.
     */
    public Transform(Vector2 position, float scaleX, float scaleY, float rotation) {
        this.position = position;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.rotation = rotation;

        this.computedPosition = new Vector2();

        this.dirty = true;
    }

    /**
     * Constructor.
     *
     * @param other The transform to copy.
     */
    public Transform(Transform other) {
        this.position = new Vector2(other.position);
        this.scaleX = other.scaleX;
        this.scaleY = other.scaleY;
        this.rotation = other.rotation;

        this.computedPosition = new Vector2();

        this.dirty = true;
    }

    /**
     * Rotate the transform to face the given position.
     *
     * @param target The coordinates to look at.
     */
    public void lookAt(Vector2 target) {
        this.rotation = MathHelper.direction(this.getWorldPosition(), target);
        this.setDirtyAndPropagate();
    }

    /**
     * Get the world position of the transform.
     *
     * @return The world position of the transform.
     */
    public Vector2 getWorldPosition() {
        if (this.dirty) {
            this.computeWorldTransform();
        }
        return this.computedPosition;
    }

    /**
     * Get the position relative to its parent (global if no parent).
     *
     * @return The position of the transform.
     */
    public Vector2 getPosition() {
        // TODO: protect scenario where transform position is changed through this access..
        return this.position;
    }

    /**
     * Sets the position of the transform.
     *
     * @param position The position of the transform.
     */
    public void setPosition(Vector2 position) {
        this.position.set(position);
        this.setDirtyAndPropagate();
    }

    /**
     * Sets the position of the transform.
     *
     * @param x The position on the x-axis.
     * @param y The position on the y-axis.
     */
    public void setPosition(float x, float y) {
        this.position.set(x, y);
        this.setDirtyAndPropagate();
    }

    /**
     * Set the position of the transform (x-axis).
     *
     * @param x The position on the x-axis.
     */
    public void setPositionX(float x) {
        this.position.setX(x);
        this.setDirtyAndPropagate();
    }

    /**
     * Set the position of the transform (y-axis).
     *
     * @param y The position on the y-axis.
     */
    public void setPositionY(float y) {
        this.position.setY(y);
        this.setDirtyAndPropagate();
    }

    /**
     * Translate the transform by the given coordinates.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     */
    public void translate(float x, float y) {
        this.position.add(x, y);
        this.setDirtyAndPropagate();
    }

    /**
     * Translate the transform by the given vector.
     *
     * @param vector The vector to translate by.
     */
    public void translate(Vector2 vector) {
        this.position.add(vector);
        this.setDirtyAndPropagate();
    }

    /**
     * Sets the scale of the transform.
     *
     * @param x The scale on the x-axis.
     * @param y The scale on the y-axis.
     */
    public void setScale(float x, float y) {
        this.setScaleX(x);
        this.setScaleY(y);
    }

    /**
     * Sets the scale of the transform.
     *
     * @param xy The scale on the x-axis and y-axis.
     */
    public void setScale(float xy) {
        this.setScaleX(xy);
        this.setScaleY(xy);
    }

    /**
     * Get the world scale of the transform (x-axis).
     *
     * @return The world scale of the transform on the x-axis.
     */
    public float getWorldScaleX() {
        if (this.dirty) {
            this.computeWorldTransform();
        }
        return computedScaleX;
    }

    /**
     * Get the scale of the transform (x-axis) relative to its parent (global if no parent).
     *
     * @return The scale of the transform on the x-axis.
     */
    public float getScaleX() {
        return scaleX;
    }

    /**
     * Set the scale of the transform (x-axis).
     *
     * @param scaleX The scale of the transform on the x-axis.
     */
    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
        this.setDirtyAndPropagate();
    }

    /**
     * Get the world scale of the transform (y-axis).
     *
     * @return The world scale of the transform on the y-axis.
     */
    public float getWorldScaleY() {
        if (this.dirty) {
            this.computeWorldTransform();
        }
        return computedScaleY;
    }

    /**
     * Get the scale of the transform (y-axis) relative to its parent (global if no parent).
     *
     * @return The scale of the transform on the y-axis.
     */
    public float getScaleY() {
        if (this.dirty) {
            this.computeWorldTransform();
        }
        return scaleY;
    }

    /**
     * Set the scale of the transform (y-axis).
     *
     * @param scaleY The scale of the transform on the y-axis.
     */
    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
        this.setDirtyAndPropagate();
    }

    /**
     * Get the world rotation of the transform.
     *
     * @return The world rotation of the transform.
     */
    public float getWorldRotation() {
        if (this.dirty) {
            this.computeWorldTransform();
        }
        return computedRotation;
    }

    /**
     * Get the rotation of the transform relative to its parent (global if no parent).
     *
     * @return The rotation of the transform.
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Set the rotation of the transform.
     *
     * @param rotation The rotation of the transform.
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
        this.setDirtyAndPropagate();
    }

    /**
     * Increments the current rotation with the given value.
     *
     * @param rotation The rotation to increment with.
     */
    public void rotate(float rotation) {
        this.rotation += rotation;
        this.setDirtyAndPropagate();
    }

    /**
     * Set the dirty flag of the transform.
     */
    protected void setDirty() {
        this.dirty = true;
    }

    /**
     * Compute the world transform coordinates. Takes into consideration the parent transformations and the local.
     */
    private void computeWorldTransform() {
        this.computedPosition.set(this.position);
        this.computedScaleX = this.scaleX;
        this.computedScaleY = this.scaleY;
        this.computedRotation = this.rotation;

        if (this.gameObject != null && this.gameObject.getParent() instanceof GameObject) {
            var parentTransform = ((GameObject) this.gameObject.getParent()).getTransform();
            this.computedPosition.add(parentTransform.getWorldPosition());
            this.computedPosition.rotateAround(parentTransform.getWorldPosition(), -parentTransform.getWorldRotation());
            this.computedScaleX *= parentTransform.getWorldScaleX();
            this.computedScaleY *= parentTransform.getWorldScaleY();
            this.computedRotation += parentTransform.getWorldRotation();
        }

        this.dirty = false;
    }

    /**
     * Set the dirty flag of the transform and propagates to the game object associated children.
     */
    private void setDirtyAndPropagate() {
        if (!this.dirty) {
            this.dirty = true;
            if (this.gameObject != null) {
                this.gameObject.propagateTransformChanged();
            }
        }
    }

    /**
     * Set the GameObject that owns this transform.
     *
     * @param gameObject The GameObject that owns this transform.
     */
    protected void setGameObject(GameObject gameObject) {
        this.gameObject = gameObject;
    }

    /**
     * Get the GameObject that owns this transform.
     *
     * @return The GameObject that owns this transform.
     */
    public GameObject getGameObject() {
        return this.gameObject;
    }
}
