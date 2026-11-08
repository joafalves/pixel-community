package org.pixel.demo.concept.spaceshooter.input;

import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

/**
 * Keyboard bindings (desktop): arrows or WASD to move, Space/J/Z to fire, X/K/Shift for a bomb, Esc/P to pause,
 * Enter/Space to confirm, Q/Backspace to go back (quit to the title from the pause menu).
 */
public final class KeyboardControls {

    private KeyboardControls() {
    }

    public static void poll(Controls controls) {
        float x = 0f, y = 0f;
        if (down(KeyboardKey.LEFT) || down(KeyboardKey.A)) {
            x -= 1f;
        }
        if (down(KeyboardKey.RIGHT) || down(KeyboardKey.D)) {
            x += 1f;
        }
        if (down(KeyboardKey.UP) || down(KeyboardKey.W)) {
            y -= 1f;
        }
        if (down(KeyboardKey.DOWN) || down(KeyboardKey.S)) {
            y += 1f;
        }
        controls.moveX = x;
        controls.moveY = y;
        controls.fire = down(KeyboardKey.SPACE) || down(KeyboardKey.J) || down(KeyboardKey.Z);
        // presses add up until the simulation consumes them (a frame may run no step)
        controls.bomb |= pressed(KeyboardKey.X) || pressed(KeyboardKey.K) || pressed(KeyboardKey.LEFT_SHIFT);
        controls.pause |= pressed(KeyboardKey.ESCAPE) || pressed(KeyboardKey.P);
        controls.confirm |= pressed(KeyboardKey.ENTER) || pressed(KeyboardKey.SPACE);
        controls.back |= pressed(KeyboardKey.Q) || pressed(KeyboardKey.BACKSPACE);
    }

    private static boolean down(KeyboardKey key) {
        return Keyboard.isKeyDown(key);
    }

    private static boolean pressed(KeyboardKey key) {
        return Keyboard.isKeyPressed(key);
    }
}
