package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.demo.learning.decs.component.PositionComponent;
import org.pixel.demo.learning.decs.component.VelocityComponent;
import org.pixel.ext.decs.Group;
import org.pixel.ext.decs.System;
import org.pixel.ext.decs.World;

/**
 * A system that updates the position of entities based on their velocity.
 */
public class MovementSystem extends System {

    private Group movingEntities;

    public MovementSystem(World world) {
        super(world);
    }

    @Override
    public void load() {
        movingEntities = world.getGroup(PositionComponent.class, VelocityComponent.class);
    }

    @Override
    public void update(DeltaTime delta) {
        for (var entity : movingEntities) {
            var position = world.getComponent(entity, PositionComponent.class);
            var velocity = world.getComponent(entity, VelocityComponent.class);

            position.getPosition().add(velocity.getVelocity().getX() * delta.getElapsed(),
                    velocity.getVelocity().getY() * delta.getElapsed());
        }
    }
}
