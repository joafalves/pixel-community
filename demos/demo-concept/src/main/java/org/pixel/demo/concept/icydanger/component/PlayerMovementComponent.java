package org.pixel.demo.concept.icydanger.component;

import org.pixel.commons.DeltaTime;
import org.pixel.demo.concept.commons.PlayerIndex;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

public class PlayerMovementComponent extends GameComponent {

    private final static float MOVE_SPEED_H = 50f;
    private final static float MOVE_SPEED_V = 100f;
    private final PlayerIndex playerIndex;

    public PlayerMovementComponent(PlayerIndex playerIndex) {
        this.playerIndex = playerIndex;
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        if (isPressingLeft()) {
            getTransform().translate(-MOVE_SPEED_H * delta.getElapsed(), 0);
        } else if (isPressingRight()) {
            getTransform().translate(MOVE_SPEED_H * delta.getElapsed(), 0);
        }

        if (isPressingUp()) {
            getTransform().translate(0, -MOVE_SPEED_V * delta.getElapsed());
        } else if (isPressingDown()) {
            getTransform().translate(0, MOVE_SPEED_V * delta.getElapsed());
        }
    }

    private boolean isPressingLeft() {
        return playerIndex == PlayerIndex.P1 ?
                Keyboard.isKeyDown(KeyboardKey.A) : Keyboard.isKeyDown(KeyboardKey.LEFT);
    }

    private boolean isPressingRight() {
        return playerIndex == PlayerIndex.P1 ?
                Keyboard.isKeyDown(KeyboardKey.D) : Keyboard.isKeyDown(KeyboardKey.RIGHT);
    }

    private boolean isPressingUp() {
        return playerIndex == PlayerIndex.P1 ?
                Keyboard.isKeyDown(KeyboardKey.W) : Keyboard.isKeyDown(KeyboardKey.UP);
    }

    private boolean isPressingDown() {
        return playerIndex == PlayerIndex.P1 ?
                Keyboard.isKeyDown(KeyboardKey.S) : Keyboard.isKeyDown(KeyboardKey.DOWN);
    }
}
