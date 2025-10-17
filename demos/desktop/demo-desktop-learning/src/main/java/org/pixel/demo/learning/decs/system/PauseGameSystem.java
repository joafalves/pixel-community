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

    private boolean isPaused = false;

    public PauseGameSystem(GameWorld world) {
        this.world = world;
    }

    @Override
    public void update(DeltaTime delta) {
        if (Keyboard.isKeyPressed(KeyboardKey.P)) {
            isPaused = !isPaused;

            for (GameSystem system : world.getSystems()) {
                // Don't disable this system or rendering systems
                if (system == this
                        || system instanceof SpriteRenderGameSystem
                        || system instanceof CanvasRenderGameSystem
                        || system instanceof HudRenderGameSystem) {
                    continue;
                }

                system.setEnabled(!isPaused);
            }

            if (isPaused) {
                java.lang.System.out.println("Game Paused");
            } else {
                java.lang.System.out.println("Game Resumed");
            }
        }
    }
}
