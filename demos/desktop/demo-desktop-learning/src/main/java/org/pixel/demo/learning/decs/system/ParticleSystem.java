package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.demo.learning.decs.component.ParticleComponent;
import org.pixel.demo.learning.decs.component.VelocityGameComponent;
import org.pixel.ext.decs.GameEntity;
import org.pixel.ext.decs.GameGroup;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;

import java.util.ArrayList;
import java.util.List;

public class ParticleSystem extends GameSystem {

    private final GameWorld world;
    private GameGroup particles;
    private final List<GameEntity> toDestroy = new ArrayList<>();

    public ParticleSystem(GameWorld world) {
        this.world = world;
    }

    @Override
    public void load() {
        particles = world.getGroup(ParticleComponent.class);
    }

    @Override
    public void update(DeltaTime delta) {
        toDestroy.clear();

        for (var entity : particles) {
            var particle = world.getComponent(entity, ParticleComponent.class);
            particle.updateLifetime(delta.getElapsed());

            // Apply friction to particle velocity
            var velocity = world.getComponent(entity, VelocityGameComponent.class);
            if (velocity != null) {
                velocity.getVelocity().multiply(0.97f);
            }

            if (particle.isExpired()) {
                toDestroy.add(entity);
            }
        }

        // Destroy expired particles after iteration
        for (var entity : toDestroy) {
            world.destroyEntity(entity);
        }
    }
}
