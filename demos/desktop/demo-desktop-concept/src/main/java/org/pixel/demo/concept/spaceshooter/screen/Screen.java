package org.pixel.demo.concept.spaceshooter.screen;

import org.pixel.demo.concept.spaceshooter.input.Controls;

/**
 * One screen of the game (title, play). The game calls {@link #update} then {@link #draw} every frame.
 */
public interface Screen {

    /** Called when the screen becomes the current one. */
    default void enter() {
    }

    /**
     * @param dt Real seconds since the last frame.
     * @return The screen to show next ({@code this} to stay).
     */
    Screen update(float dt, Controls controls);

    /** Record this frame's drawing (the game renders the pipeline afterwards). */
    void draw();
}
