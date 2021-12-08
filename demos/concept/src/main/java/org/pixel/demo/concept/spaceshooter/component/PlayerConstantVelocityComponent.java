package org.pixel.demo.concept.spaceshooter.component;

import org.pixel.commons.DeltaTime;
import org.pixel.demo.concept.common.GameComponent;
import org.pixel.demo.concept.spaceshooter.game.PlayerSprite;
import org.pixel.math.Vector2;

public class PlayerConstantVelocityComponent extends GameComponent {

    private final Vector2 velocity;

    public PlayerConstantVelocityComponent(Vector2 velocity) {
        this.velocity = velocity;
    }

    @Override
    public void update(DeltaTime delta) {
        if (Boolean.FALSE.equals(parent.getAttributeMap().getBoolean(PlayerSprite.IS_MOVING_FORWARD_ATTR))) {
            parent.getTransform().getPosition().add(
                    velocity.getX() * delta.getElapsed(),
                    velocity.getY() * delta.getElapsed());
        }
    }
}
