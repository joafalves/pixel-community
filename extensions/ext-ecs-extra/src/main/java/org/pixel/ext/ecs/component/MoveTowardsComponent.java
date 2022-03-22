package org.pixel.ext.ecs.component;

import org.pixel.commons.DeltaTime;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

public class MoveTowardsComponent extends GameComponent {

    private Vector2 position;
    private float minimumDistance;
    private float speed;
    private boolean smoothRotation;

    /**
     * Constructor.
     *
     * @param position        The position to move towards.
     * @param speed           The speed to move at.
     * @param minimumDistance The minimum distance required for the entity to move towards the position.
     * @param smoothRotation  Whether to smooth rotation.
     */
    public MoveTowardsComponent(Vector2 position, float speed, float minimumDistance, boolean smoothRotation) {
        this.position = position;
        this.speed = speed;
        this.minimumDistance = minimumDistance;
        this.smoothRotation = smoothRotation;
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        float distance = getTransform().getWorldPosition().distanceTo(position);
        if (distance < minimumDistance) {
            return; // We're already there.
        }

        if (smoothRotation) {
            float direction = MathHelper.direction(getTransform().getWorldPosition(), position);
            if (Math.abs(getTransform().getRotation() - direction) >= MathHelper.PI) {
                direction += (MathHelper.PI2 * (getTransform().getRotation() < 0 ? -1 : 1));
            }

            getTransform().setRotation(MathHelper.linearInterpolation(
                    getTransform().getRotation(), direction, 3f * delta.getElapsed())
            );

        } else {
            getTransform().lookAt(position);
        }

        getTransform().translate(
                MathHelper.cos(getTransform().getRotation()) * speed * delta.getElapsed(),
                MathHelper.sin(getTransform().getRotation()) * speed * delta.getElapsed()
        );
    }

    /**
     * Get the position to move towards.
     *
     * @return The position to move towards.
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * Set the position to move towards.
     *
     * @param position The position to move towards.
     */
    public void setPosition(Vector2 position) {
        this.position = position;
    }

    /**
     * Get the speed to move at.
     *
     * @return The speed to move at.
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Set the speed to move at.
     *
     * @param speed The speed to move at.
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Whether to smooth rotation.
     *
     * @return Whether to smooth rotation.
     */
    public boolean isSmoothRotation() {
        return smoothRotation;
    }

    /**
     * Set whether to smooth rotation.
     *
     * @param smoothRotation Whether to smooth rotation.
     */
    public void setSmoothRotation(boolean smoothRotation) {
        this.smoothRotation = smoothRotation;
    }

    /**
     * Get Minimum distance required for the entity to move towards the position.
     *
     * @return Minimum distance required for the entity to move towards the position.
     */
    public float getMinimumDistance() {
        return minimumDistance;
    }

    /**
     * Set Minimum distance required for the entity to move towards the position.
     *
     * @param minimumDistance Minimum distance required for the entity to move towards the position.
     */
    public void setMinimumDistance(float minimumDistance) {
        this.minimumDistance = minimumDistance;
    }
}
