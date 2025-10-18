package org.pixel.demo.learning.decs.system;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.event.EventBus;
import org.pixel.demo.learning.decs.component.*;
import org.pixel.demo.learning.decs.event.EntityDeathEvent;
import org.pixel.demo.learning.decs.event.ScoreEvent;
import org.pixel.ext.decs.GameEntity;
import org.pixel.ext.decs.GameGroup;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;
import org.pixel.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class HealthSystem extends GameSystem {

    private final GameWorld world;
    private final EventBus eventBus;
    private GameGroup healthEntities;
    private GameGroup projectiles;
    private final List<GameEntity> deadEntities = new ArrayList<>();
    private final List<GameEntity> projectilesToDestroy = new ArrayList<>();

    public HealthSystem(GameWorld world, EventBus eventBus) {
        this.world = world;
        this.eventBus = eventBus;
    }

    @Override
    public void load() {
        healthEntities = world.getGroup(HealthComponent.class);
        projectiles = world.getGroup(ProjectileComponent.class, CollisionGameComponent.class);
    }

    @Override
    public void update(DeltaTime delta) {
        deadEntities.clear();
        projectilesToDestroy.clear();

        // Update invincibility timers
        for (var entity : healthEntities) {
            var health = world.getComponent(entity, HealthComponent.class);
            health.updateInvincibility(delta.getElapsed());

            // Check for death
            if (!health.isAlive()) {
                deadEntities.add(entity);
            }
        }

        // Check projectile collisions
        for (var projectile : projectiles) {
            var projComp = world.getComponent(projectile, ProjectileComponent.class);
            var projCollision = world.getComponent(projectile, CollisionGameComponent.class);

            boolean projectileHit = false;
            for (var target : healthEntities) {
                // Don't hit owner
                if (target.equals(projComp.getOwner())) continue;

                // Player projectiles hit enemies, enemy projectiles hit player
                boolean targetIsEnemy = world.hasComponent(target, EnemyComponent.class);
                boolean targetIsPlayer = world.hasComponent(target, PlayerGameComponent.class);

                if ((projComp.isEnemyProjectile() && targetIsPlayer) ||
                    (!projComp.isEnemyProjectile() && targetIsEnemy)) {

                    var targetCollision = world.getComponent(target, CollisionGameComponent.class);
                    if (targetCollision != null && projCollision.getBoundingBox().overlaps(targetCollision.getBoundingBox())) {
                        var health = world.getComponent(target, HealthComponent.class);
                        health.damage(projComp.getDamage());

                        // Mark projectile for removal
                        projectilesToDestroy.add(projectile);
                        projectileHit = true;

                        // Create hit particles
                        var pos = world.getComponent(target, PositionGameComponent.class);
                        createHitParticles(pos.getPosition().getX() + 16, pos.getPosition().getY() + 16);
                        break;
                    }
                }
            }

            if (projectileHit) {
                break;
            }
        }

        // Handle all deaths after iteration
        for (var entity : deadEntities) {
            handleDeath(entity);
        }

        // Destroy all hit projectiles after iteration
        for (var projectile : projectilesToDestroy) {
            world.destroyEntity(projectile);
        }
    }

    private void handleDeath(GameEntity entity) {
        var position = world.getComponent(entity, PositionGameComponent.class);
        boolean isEnemy = world.hasComponent(entity, EnemyComponent.class);

        // Fire death event
        eventBus.publish(new EntityDeathEvent(entity, position.getPosition().getX() + 16,
                position.getPosition().getY() + 16, isEnemy));

        // Award score for enemy kills
        if (isEnemy) {
            var scoreValue = world.getComponent(entity, ScoreValueComponent.class);
            if (scoreValue != null) {
                eventBus.publish(new ScoreEvent(scoreValue.getPoints(),
                        position.getPosition().getX() + 16, position.getPosition().getY() + 16));
            }
        }

        // Create explosion particles
        createExplosionParticles(position.getPosition().getX() + 16, position.getPosition().getY() + 16, isEnemy);

        world.destroyEntity(entity);
    }

    private void createExplosionParticles(float x, float y, boolean isEnemy) {
        Color baseColor = isEnemy ? new Color(1f, 0.5f, 0) : new Color(0.8f, 0.2f, 0.2f);

        // Layer 1: Bright white flash particles (very fast, small, short-lived)
        for (int i = 0; i < 12; i++) {
            GameEntity particle = world.createEntity();
            world.addComponent(particle, new ParticleComponent(0.15f, Color.WHITE, 4, true));

            float angle = (float) (Math.random() * Math.PI * 2);
            float speed = MathHelper.random(250f, 400f); // Very fast
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed;

            world.addComponent(particle, new PositionGameComponent(x, y));
            VelocityGameComponent velocity = new VelocityGameComponent();
            velocity.getVelocity().set(vx, vy);
            world.addComponent(particle, velocity);
        }

        // Layer 2: Bright core explosion (fast, medium size)
        Color brightCore = isEnemy ? new Color(1f, 0.9f, 0.3f) : new Color(1f, 0.5f, 0.5f);
        for (int i = 0; i < 20; i++) {
            GameEntity particle = world.createEntity();
            world.addComponent(particle, new ParticleComponent(0.4f, brightCore, 6, true));

            float angle = (float) (Math.random() * Math.PI * 2);
            float speed = MathHelper.random(150f, 250f);
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed;

            world.addComponent(particle, new PositionGameComponent(x, y));
            VelocityGameComponent velocity = new VelocityGameComponent();
            velocity.getVelocity().set(vx, vy);
            world.addComponent(particle, velocity);
        }

        // Layer 3: Main explosion particles (medium speed)
        for (int i = 0; i < 25; i++) {
            GameEntity particle = world.createEntity();
            world.addComponent(particle, new ParticleComponent(0.6f, baseColor, 8, true));

            float angle = (float) (Math.random() * Math.PI * 2);
            float speed = MathHelper.random(80f, 150f);
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed;

            world.addComponent(particle, new PositionGameComponent(x, y));
            VelocityGameComponent velocity = new VelocityGameComponent();
            velocity.getVelocity().set(vx, vy);
            world.addComponent(particle, velocity);
        }

        // Layer 4: Slower outer particles with varied colors (debris/smoke)
        Color darkColor = isEnemy ? new Color(0.4f, 0.2f, 0) : new Color(0.4f, 0.1f, 0.1f);
        for (int i = 0; i < 15; i++) {
            GameEntity particle = world.createEntity();
            // Mix of base color and darker color
            Color particleColor = MathHelper.random(0, 2) == 0 ? baseColor : darkColor;
            world.addComponent(particle, new ParticleComponent(1.0f, particleColor, 10, true));

            float angle = (float) (Math.random() * Math.PI * 2);
            float speed = MathHelper.random(30f, 80f);
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed;

            world.addComponent(particle, new PositionGameComponent(x + MathHelper.random(-5, 5), y + MathHelper.random(-5, 5)));
            VelocityGameComponent velocity = new VelocityGameComponent();
            velocity.getVelocity().set(vx, vy);
            world.addComponent(particle, velocity);
        }

        // Layer 5: Sparks (very fast streaks in random directions)
        for (int i = 0; i < 8; i++) {
            GameEntity particle = world.createEntity();
            Color sparkColor = new Color(1f, 1f, 0.8f);
            world.addComponent(particle, new ParticleComponent(0.25f, sparkColor, 3, true));

            float angle = (float) (Math.random() * Math.PI * 2);
            float speed = MathHelper.random(300f, 500f);
            float vx = (float) Math.cos(angle) * speed;
            float vy = (float) Math.sin(angle) * speed;

            world.addComponent(particle, new PositionGameComponent(x, y));
            VelocityGameComponent velocity = new VelocityGameComponent();
            velocity.getVelocity().set(vx, vy);
            world.addComponent(particle, velocity);
        }
    }

    private void createHitParticles(float x, float y) {
        for (int i = 0; i < 5; i++) {
            GameEntity particle = world.createEntity();
            world.addComponent(particle, new ParticleComponent(0.3f, Color.WHITE, 4, true));
            world.addComponent(particle, new PositionGameComponent(
                    x + MathHelper.random(-8, 8), y + MathHelper.random(-8, 8)));
        }
    }
}
