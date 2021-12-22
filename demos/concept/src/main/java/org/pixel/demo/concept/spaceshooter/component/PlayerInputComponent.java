/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.concept.spaceshooter.component;

import org.pixel.commons.DeltaTime;
import org.pixel.demo.concept.spaceshooter.SpaceShooterGame;
import org.pixel.demo.concept.spaceshooter.entity.PlayerSprite;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

public class PlayerInputComponent extends GameComponent {

    private final float SPEED_Y = 100.f;
    private final float SPEED_X = 150.f;
    private final float SHOOT_DELAY_MS = 100;

    private float shootElapsed = 0f;

    @Override
    public void update(DeltaTime delta) {
        handleMovement(delta);
        handleShooting(delta);
    }

    private void handleShooting(DeltaTime delta) {
        shootElapsed += delta.getElapsedMs();
        if (Keyboard.isKeyDown(KeyboardKey.SPACE) && shootElapsed > SHOOT_DELAY_MS) {
            shootElapsed = 0f;
            SpaceShooterGame.EVENT.publish("player.shoot", null);
        }
    }

    private void handleMovement(DeltaTime delta) {
        var keyboardState = Keyboard.getState();
        if (keyboardState.isKeyDown(KeyboardKey.W)) { // forward
            getGameObject().getTransform().translate(0, -SPEED_Y * delta.getElapsed());
            getGameObject().getAttributeMap().put(PlayerSprite.IS_MOVING_FORWARD_ATTR, true);
        } else {
            getGameObject().getAttributeMap().put(PlayerSprite.IS_MOVING_FORWARD_ATTR, false);
        }

        if (keyboardState.isKeyDown(KeyboardKey.A)) { // left
            getGameObject().getTransform().translate(-SPEED_X * delta.getElapsed(), 0);

        } else if (keyboardState.isKeyDown(KeyboardKey.D)) { // right
            getGameObject().getTransform().translate(SPEED_X * delta.getElapsed(), 0);
        }
    }
}
