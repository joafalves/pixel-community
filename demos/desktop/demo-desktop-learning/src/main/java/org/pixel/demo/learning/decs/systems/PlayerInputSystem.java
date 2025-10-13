package org.pixel.demo.learning.decs.systems;

import org.pixel.commons.DeltaTime;
import org.pixel.demo.learning.decs.components.PlayerComponent;
import org.pixel.demo.learning.decs.components.VelocityComponent;
import org.pixel.ext.decs.Group;
import org.pixel.ext.decs.System;
import org.pixel.ext.decs.World;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

/**
 * A system that handles player input and updates the velocity of the player entity.
 */
public class PlayerInputSystem extends System {

    private static final float PLAYER_SPEED = 100f;

    private Group players;

    public PlayerInputSystem(World world) {
        super(world);
    }

    @Override
    public void load() {
        players = world.getGroup(PlayerComponent.class, VelocityComponent.class);
    }

    @Override
    public void update(DeltaTime delta) {
        for (var entity : players) {
            var velocity = world.getComponent(entity, VelocityComponent.class);
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
        }
    }
}
