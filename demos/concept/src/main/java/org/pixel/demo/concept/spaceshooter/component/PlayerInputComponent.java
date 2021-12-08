/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.concept.spaceshooter.component;

import org.pixel.commons.DeltaTime;
import org.pixel.demo.concept.common.GameComponent;
import org.pixel.demo.concept.spaceshooter.game.PlayerSprite;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

public class PlayerInputComponent extends GameComponent {

    private final float SPEED_Y = 100.f;
    private final float SPEED_X = 150.f;

    @Override
    public void update(DeltaTime delta) {
        handleMovement(delta);
    }

    private void handleMovement(DeltaTime delta) {
        if (Keyboard.isKeyDown(KeyboardKey.W)) { // forward
            parent.getTransform().translate(0, -SPEED_Y * delta.getElapsed());
            parent.getAttributeMap().put(PlayerSprite.IS_MOVING_FORWARD_ATTR, true);
        } else {
            parent.getAttributeMap().put(PlayerSprite.IS_MOVING_FORWARD_ATTR, false);
        }

        if (Keyboard.isKeyDown(KeyboardKey.A)) { // left
            parent.getTransform().translate(-SPEED_X * delta.getElapsed(), 0);

        } else if (Keyboard.isKeyDown(KeyboardKey.D)) { // right
            parent.getTransform().translate(SPEED_X * delta.getElapsed(), 0);
        }
    }
}
