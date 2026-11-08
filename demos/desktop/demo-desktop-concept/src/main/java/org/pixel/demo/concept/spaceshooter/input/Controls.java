package org.pixel.demo.concept.spaceshooter.input;

/**
 * What the player asks for this frame, independent of the device (keyboard, autopilot, tests). "Pressed" flags are
 * true for one frame; the simulation consumes them in its first step.
 */
public final class Controls {

    /** Movement, -1 to 1 on each axis (y down). */
    public float moveX, moveY;
    /** Fire is held. */
    public boolean fire;
    /** Bomb, pause, confirm (start, continue) and back were pressed this frame. */
    public boolean bomb, pause, confirm, back;

    public void clear() {
        moveX = moveY = 0f;
        fire = bomb = pause = confirm = back = false;
    }

    /** Forget the one-frame presses (after the first simulation step used them). */
    public void consumePresses() {
        bomb = pause = confirm = back = false;
    }
}
