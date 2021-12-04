package org.pixel.concept.spaceshooter.component;

import org.pixel.commons.DeltaTime;
import org.pixel.concept.common.GameComponent;
import org.pixel.math.Boundary;
import org.pixel.math.MathHelper;

public class PlayerBoundaryComponent extends GameComponent {

    private final Boundary playerBoundary;

    public PlayerBoundaryComponent(Boundary playerBoundary) {
        this.playerBoundary = playerBoundary;
    }

    @Override
    public void update(DeltaTime delta) {
        parent.getTransform().setPosition(
                MathHelper.clamp(parent.getTransform().getPosition().getX(), playerBoundary.getLeft(), playerBoundary.getRight()),
                MathHelper.clamp(parent.getTransform().getPosition().getY(), playerBoundary.getTop(), playerBoundary.getBottom())
        );
    }
}
