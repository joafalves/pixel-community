package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.demo.learning.decs.component.PositionGameComponent;
import org.pixel.demo.learning.decs.component.VelocityGameComponent;
import org.pixel.ext.decs.GameGroup;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;

/**
 * A system that updates the position of entities based on their velocity.
 */
public class MovementGameSystem extends GameSystem {

    private final GameWorld world;

    private GameGroup movingEntities;

    public MovementGameSystem(GameWorld world) {
        this.world = world;
    }

    @Override
    public void load() {
        movingEntities = world.getGroup(PositionGameComponent.class, VelocityGameComponent.class);
    }

    @Override
    public void update(DeltaTime delta) {
        for (var entity : movingEntities) {
            var position = world.getComponent(entity, PositionGameComponent.class);
            var velocity = world.getComponent(entity, VelocityGameComponent.class);

            position.getPosition().add(velocity.getVelocity().getX() * delta.getElapsed(),
                    velocity.getVelocity().getY() * delta.getElapsed());
        }
    }
}
