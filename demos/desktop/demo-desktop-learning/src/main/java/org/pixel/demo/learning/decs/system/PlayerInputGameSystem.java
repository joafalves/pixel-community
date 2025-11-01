package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.demo.learning.decs.component.PlayerGameComponent;
import org.pixel.demo.learning.decs.component.VelocityGameComponent;
import org.pixel.ext.decs.GameGroup;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

/**
 * A system that handles player input and updates the velocity of the player entity.
 */
public class PlayerInputGameSystem extends GameSystem {

    private static final float PLAYER_SPEED = 200f; // Faster for action gameplay

    private final GameWorld world;

    private GameGroup players;

    public PlayerInputGameSystem(GameWorld world) {
        this.world = world;
    }

    @Override
    public void load() {
        players = world.getGroup(PlayerGameComponent.class, VelocityGameComponent.class);
    }

    @Override
    public void update(DeltaTime delta) {
        for (var entity : players) {
            var velocity = world.getComponent(entity, VelocityGameComponent.class);
            velocity.getVelocity().set(0, 0);

            if (Keyboard.isKeyDown(KeyboardKey.W)) {
                velocity.getVelocity().add(0, -PLAYER_SPEED);
            }
            if (Keyboard.isKeyDown(KeyboardKey.S)) {
                velocity.getVelocity().add(0, PLAYER_SPEED);
            }
            if (Keyboard.isKeyDown(KeyboardKey.A)) {
                velocity.getVelocity().add(-PLAYER_SPEED, 0);
            }
            if (Keyboard.isKeyDown(KeyboardKey.D)) {
                velocity.getVelocity().add(PLAYER_SPEED, 0);
            }

            // Normalize diagonal movement
            if (velocity.getVelocity().length() > PLAYER_SPEED) {
                velocity.getVelocity().normalize();
                velocity.getVelocity().multiply(PLAYER_SPEED);
            }
        }
    }
}
