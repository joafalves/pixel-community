package org.pixel.demo.learning.decs.system;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.demo.learning.decs.component.*;
import org.pixel.ext.decs.GameEntity;
import org.pixel.ext.decs.GameGroup;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.input.mouse.Mouse;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

public class WeaponSystem extends GameSystem {

    private final GameWorld world;
    private GameGroup shooters; // Entities with weapons (player or enemies)

    public WeaponSystem(GameWorld world) {
        this.world = world;
    }

    @Override
    public void load() {
        shooters = world.getGroup(WeaponComponent.class, PositionGameComponent.class);
    }

    @Override
    public void update(DeltaTime delta) {
        for (var entity : shooters) {
            var weapon = world.getComponent(entity, WeaponComponent.class);
            weapon.updateCooldown(delta.getElapsed());

            // Player shooting (with mouse or arrow keys)
            if (world.hasComponent(entity, PlayerGameComponent.class)) {
                handlePlayerShooting(entity, weapon);
            }
            // Enemy shooting
            else if (world.hasComponent(entity, EnemyComponent.class)) {
                handleEnemyShooting(entity, weapon);
            }
        }
    }

    private void handlePlayerShooting(GameEntity entity, WeaponComponent weapon) {
        if (!weapon.canFire()) return;

        var position = world.getComponent(entity, PositionGameComponent.class);
        Vector2 direction = new Vector2();

        // Mouse-aimed shooting (left click)
        if (Mouse.isMouseButtonDown(org.pixel.input.mouse.MouseButton.LEFT)) {
            Vector2 mousePos = Mouse.getPosition();
            Vector2 playerCenter = new Vector2(position.getPosition().getX() + 16, position.getPosition().getY() + 16);
            direction.set(mousePos.getX() - playerCenter.getX(), mousePos.getY() - playerCenter.getY());
            direction.normalize();

            createProjectile(position.getPosition().getX() + 16, position.getPosition().getY() + 16,
                    direction, weapon, entity, false);
            weapon.fire();
        }
        // Arrow key shooting (8-directional)
        else {
            if (Keyboard.isKeyDown(KeyboardKey.UP)) direction.add(0, -1);
            if (Keyboard.isKeyDown(KeyboardKey.DOWN)) direction.add(0, 1);
            if (Keyboard.isKeyDown(KeyboardKey.LEFT)) direction.add(-1, 0);
            if (Keyboard.isKeyDown(KeyboardKey.RIGHT)) direction.add(1, 0);

            if (direction.length() > 0) {
                direction.normalize();
                createProjectile(position.getPosition().getX() + 16, position.getPosition().getY() + 16,
                        direction, weapon, entity, false);
                weapon.fire();
            }
        }
    }

    private void handleEnemyShooting(GameEntity entity, WeaponComponent weapon) {
        if (!weapon.canFire()) return;

        var ai = world.getComponent(entity, AIComponent.class);
        if (ai == null || ai.getTarget() == null || !ai.canAct()) return;

        var position = world.getComponent(entity, PositionGameComponent.class);
        var targetPos = world.getComponent(ai.getTarget(), PositionGameComponent.class);

        if (targetPos != null) {
            Vector2 direction = new Vector2(
                    targetPos.getPosition().getX() - position.getPosition().getX(),
                    targetPos.getPosition().getY() - position.getPosition().getY()
            );
            direction.normalize();

            createProjectile(position.getPosition().getX() + 12, position.getPosition().getY() + 12,
                    direction, weapon, entity, true);
            weapon.fire();
        }
    }

    private void createProjectile(float x, float y, Vector2 direction, WeaponComponent weapon,
                                   GameEntity owner, boolean isEnemyProjectile) {
        GameEntity projectile = world.createEntity();
        world.addComponent(projectile, new ProjectileComponent(weapon.getProjectileDamage(),
                weapon.getProjectileLifetime(), owner, isEnemyProjectile));
        world.addComponent(projectile, new PositionGameComponent(x, y));

        VelocityGameComponent velocity = new VelocityGameComponent();
        velocity.getVelocity().set(direction.getX() * weapon.getProjectileSpeed(),
                                    direction.getY() * weapon.getProjectileSpeed());
        world.addComponent(projectile, velocity);

        // Collision for projectiles (small hitbox)
        world.addComponent(projectile, new CollisionGameComponent(x, y, 4, 4));

        // Visual trail effect
        createTrailParticle(x, y, isEnemyProjectile);
    }

    private void createTrailParticle(float x, float y, boolean isEnemy) {
        GameEntity particle = world.createEntity();
        Color color = isEnemy ? new Color(1f, 0.3f, 0.3f) : new Color(0.3f, 0.8f, 1f);
        world.addComponent(particle, new ParticleComponent(0.3f, color, 3, true));
        world.addComponent(particle, new PositionGameComponent(x - MathHelper.random(-2, 2),
                                                                y - MathHelper.random(-2, 2)));
    }
}
