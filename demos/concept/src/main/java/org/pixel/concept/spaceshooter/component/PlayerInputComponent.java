/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.concept.spaceshooter.component;

import org.pixel.concept.common.GameComponent;
import org.pixel.commons.DeltaTime;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

public class PlayerInputComponent extends GameComponent {

    private final float SPEED_Y = 50.f;
    private final float SPEED_X = 65.f;

    @Override
    public void update(DeltaTime delta) {
        handleMovement(delta);
    }

    private void handleMovement(DeltaTime delta) {
        if (Keyboard.isKeyDown(KeyboardKey.W)) { // forward
            parent.getTransform().translate(0, -SPEED_Y * delta.getElapsed());

        } else if (Keyboard.isKeyDown(KeyboardKey.S)) { // forward
            parent.getTransform().translate(0, SPEED_Y * delta.getElapsed());
        }

        if (Keyboard.isKeyDown(KeyboardKey.A)) { // forward
            parent.getTransform().translate(-SPEED_X * delta.getElapsed(), 0);

        } else if (Keyboard.isKeyDown(KeyboardKey.D)) { // forward
            parent.getTransform().translate(SPEED_X * delta.getElapsed(), 0);
        }
    }
}
