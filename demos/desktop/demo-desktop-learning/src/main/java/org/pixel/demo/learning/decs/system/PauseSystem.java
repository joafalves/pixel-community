package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.ext.decs.System;
import org.pixel.ext.decs.World;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

/**
 * A system that can pause and un-pause other systems in the world.
 */
public class PauseSystem extends System {

    private boolean isPaused = false;

    public PauseSystem(World world) {
        super(world);
    }

    @Override
    public void update(DeltaTime delta) {
        if (Keyboard.isKeyPressed(KeyboardKey.P)) {
            isPaused = !isPaused;

            for (System system : world.getSystems()) {
                // Don't disable this system or rendering systems
                if (system == this || system instanceof SpriteRenderSystem || system instanceof HudRenderSystem) {
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
