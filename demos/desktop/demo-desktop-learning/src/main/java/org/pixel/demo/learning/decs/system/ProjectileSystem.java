package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.demo.learning.decs.component.*;
import org.pixel.ext.decs.GameEntity;
import org.pixel.ext.decs.GameGroup;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;

import java.util.ArrayList;
import java.util.List;

public class ProjectileSystem extends GameSystem {

    private final GameWorld world;
    private GameGroup projectiles;
    private final List<GameEntity> toDestroy = new ArrayList<>();

    public ProjectileSystem(GameWorld world) {
        this.world = world;
    }

    @Override
    public void load() {
        projectiles = world.getGroup(ProjectileComponent.class);
    }

    @Override
    public void update(DeltaTime delta) {
        toDestroy.clear();

        for (var entity : projectiles) {
            var projectile = world.getComponent(entity, ProjectileComponent.class);
            projectile.updateLifetime(delta.getElapsed());

            if (projectile.isExpired()) {
                toDestroy.add(entity);
            }
        }

        // Destroy expired projectiles after iteration
        for (var entity : toDestroy) {
            world.destroyEntity(entity);
        }
    }
}
