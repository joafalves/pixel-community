package org.pixel.demo.concept.icydanger.component;

import lombok.RequiredArgsConstructor;
import org.pixel.commons.DeltaTime;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

@RequiredArgsConstructor
public class BallMovementComponent extends GameComponent {

    private final float maxDistance;
    private final float speed;
    private final float heightVariation;

    private Vector2 initialPosition = null;
    private boolean finished = false;

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        if (finished) {
            return;
        }

        if (initialPosition == null) {
            initialPosition = new Vector2(getTransform().getPosition());
        }

        float newX = getTransform().getPosition().getX() + speed * delta.getElapsed();
        float distance = newX - initialPosition.getX();
        if (MathHelper.abs(distance) >= maxDistance) {
            getTransform().setPosition(initialPosition.getX() + maxDistance * (speed < 0 ? -1f : 1f),
                    initialPosition.getY());
            getGameObject().getComponent(BallCollisionComponent.class).dispose();
            finished = true;
            return;
        }

        float newY = initialPosition.getY() + MathHelper.sin(distance * MathHelper.PI / maxDistance)
                * heightVariation * (speed > 0 ? -1f : 1f);

        getGameObject().getChild("ballShadow").getTransform().setPositionY(initialPosition.getY() - newY);

        getTransform().setPosition(newX, newY);
    }
}
