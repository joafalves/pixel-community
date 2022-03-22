package org.pixel.ext.ecs.component;

import org.pixel.commons.DeltaTime;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.math.Vector2;

/**
 * A component that adds a constant velocity to the game entity.
 */
public class ConstantVelocityComponent extends GameComponent {

    private Vector2 velocity;

    /**
     * Constructor.
     *
     * @param velocity The constant velocity to be applied on the associated object.
     */
    public ConstantVelocityComponent(Vector2 velocity) {
        super(ConstantVelocityComponent.class.getSimpleName());
        this.velocity = velocity;
    }

    @Override
    public void update(DeltaTime delta) {
        getTransform().translate(velocity.getX() * delta.getElapsed(), velocity.getY() * delta.getElapsed());
    }

    /**
     * Get the constant velocity.
     *
     * @return The constant velocity.
     */
    public Vector2 getVelocity() {
        return velocity;
    }

    /**
     * Set the constant velocity.
     *
     * @param velocity The constant velocity to be applied on the associated object.
     */
    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }
}
