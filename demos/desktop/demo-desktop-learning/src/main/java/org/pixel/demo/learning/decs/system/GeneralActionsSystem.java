package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.core.Game;
import org.pixel.ext.decs.System;
import org.pixel.ext.decs.World;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

public class GeneralActionsSystem extends System {
    /**
     * Constructor.
     *
     * @param world The world this system belongs to.
     */
    public GeneralActionsSystem(World world) {
        super(world);
    }

    @Override
    public void update(DeltaTime delta) {
        if (Keyboard.isKeyPressed(KeyboardKey.ESCAPE)) {
            world.getProperties().get(Game.class).dispose();
        }
    }
}
