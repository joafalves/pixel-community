package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.demo.learning.decs.component.CollisionComponent;
import org.pixel.demo.learning.decs.component.PositionComponent;
import org.pixel.ext.decs.Group;
import org.pixel.ext.decs.System;
import org.pixel.ext.decs.World;

/**
 * A system that updates the position of collision boxes based on the entity's position.
 */
public class CollisionSystem extends System {

    private Group collidables;

    public CollisionSystem(World world) {
        super(world);
    }

    @Override
    public void load() {
        collidables = world.getGroup(PositionComponent.class, CollisionComponent.class);
    }

    @Override
    public void update(DeltaTime delta) {
        for (var entity : collidables) {
            var position = world.getComponent(entity, PositionComponent.class);
            var collision = world.getComponent(entity, CollisionComponent.class);

            collision.getBoundingBox().setX(position.getPosition().getX());
            collision.getBoundingBox().setY(position.getPosition().getY());
        }
    }
}
