package org.pixel.demo.concept.commons.component;

import org.pixel.commons.DeltaTime;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.math.Boundary;
import org.pixel.math.MathHelper;

public class PlayerBoundaryComponent extends GameComponent {

    private final Boundary playerBoundary;

    public PlayerBoundaryComponent(Boundary playerBoundary) {
        this.playerBoundary = playerBoundary;
    }

    @Override
    public void update(DeltaTime delta) {
        getGameObject().getTransform().setPosition(
                MathHelper.clamp(getGameObject().getTransform().getWorldPosition().getX(), playerBoundary.getLeft(),
                        playerBoundary.getRight()),
                MathHelper.clamp(getGameObject().getTransform().getWorldPosition().getY(), playerBoundary.getTop(),
                        playerBoundary.getBottom())
        );
    }
}
