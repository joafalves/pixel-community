package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

/**
 * A system that can pause and un-pause other systems in the world.
 */
public class PauseGameSystem extends GameSystem {

    private final GameWorld world;
    private final NeonHudSystem hudSystem;
    private boolean isPaused = false;

    public PauseGameSystem(GameWorld world, NeonHudSystem hudSystem) {
        this.world = world;
        this.hudSystem = hudSystem;
    }

    @Override
    public void update(DeltaTime delta) {
        if (Keyboard.isKeyPressed(KeyboardKey.P)) {
            isPaused = !isPaused;

            for (GameSystem system : world.getSystems()) {
                // Don't disable this system or rendering systems
                if (system == this
                        || system instanceof NeonRenderSystem
                        || system instanceof NeonHudSystem) {
                    continue;
                }

                system.setEnabled(!isPaused);
            }

            // Update HUD to show paused overlay
            if (hudSystem != null) {
                hudSystem.setPaused(isPaused);
            }

            if (isPaused) {
                java.lang.System.out.println("Game Paused");
            } else {
                java.lang.System.out.println("Game Resumed");
            }
        }
    }
}
