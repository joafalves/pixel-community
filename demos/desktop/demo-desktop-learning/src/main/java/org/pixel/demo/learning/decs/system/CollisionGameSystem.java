package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.demo.learning.decs.component.CollisionGameComponent;
import org.pixel.demo.learning.decs.component.PositionGameComponent;
import org.pixel.ext.decs.GameGroup;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;

/**
 * A system that updates the position of collision boxes based on the entity's position.
 */
public class CollisionGameSystem extends GameSystem {

    private GameGroup collidables;
    private final GameWorld world;

    public CollisionGameSystem(GameWorld world) {
        this.world = world;
    }

    @Override
    public void load() {
        collidables = world.getGroup(PositionGameComponent.class, CollisionGameComponent.class);
    }

    @Override
    public void update(DeltaTime delta) {
        for (var entity : collidables) {
            var position = world.getComponent(entity, PositionGameComponent.class);
            var collision = world.getComponent(entity, CollisionGameComponent.class);

            collision.getBoundingBox().setX(position.getPosition().getX());
            collision.getBoundingBox().setY(position.getPosition().getY());
        }
    }
}
