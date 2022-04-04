/*
 * This software is available under Apache License
 * Copyright (c)
 */

package org.pixel.demo.concept.performance.component;

import org.pixel.commons.DeltaTime;
import org.pixel.ext.ecs.component.ConstantVelocityComponent;
import org.pixel.math.Boundary;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

public class ConstantVelocityBoundComponent extends ConstantVelocityComponent {

    private final Boundary screenBoundary;

    public ConstantVelocityBoundComponent(Vector2 velocity, Boundary screenBoundary) {
        super(velocity);
        this.screenBoundary = screenBoundary;
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        if (getTransform().getPosition().getX() > screenBoundary.getWidth()
                || getTransform().getPosition().getX() < 0) {
            clampPosition();
            getVelocity().setX(getVelocity().getX() * -1);

        } else if (getTransform().getPosition().getY() > screenBoundary.getHeight()
                || getTransform().getPosition().getY() < 0) {
            clampPosition();
            getVelocity().setY(getVelocity().getY() * -1);
        }
    }

    private void clampPosition() {
        getTransform().setPosition(
                MathHelper.clamp(getGameObject().getTransform().getWorldPosition().getX(), screenBoundary.getLeft(),
                        screenBoundary.getRight()),
                MathHelper.clamp(getGameObject().getTransform().getWorldPosition().getY(), screenBoundary.getTop(),
                        screenBoundary.getBottom())
        );
    }
}
