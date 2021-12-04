package org.pixel.concept.spaceshooter.component;

import static org.pixel.concept.spaceshooter.game.PlayerSprite.IS_MOVING_FORWARD_ATTR;

import org.pixel.commons.DeltaTime;
import org.pixel.concept.common.GameComponent;
import org.pixel.math.Vector2;

public class PlayerConstantVelocityComponent extends GameComponent {

    private final Vector2 velocity;

    public PlayerConstantVelocityComponent(Vector2 velocity) {
        this.velocity = velocity;
    }

    @Override
    public void update(DeltaTime delta) {
        if (Boolean.FALSE.equals(parent.getAttributeMap().getBoolean(IS_MOVING_FORWARD_ATTR))) {
            parent.getTransform().getPosition().add(
                    velocity.getX() * delta.getElapsed(),
                    velocity.getY() * delta.getElapsed());
        }
    }
}
