package org.pixel.demo.learning.decs.system;

import org.pixel.commons.DeltaTime;
import org.pixel.core.Game;
import org.pixel.ext.decs.GameSystem;
import org.pixel.ext.decs.GameWorld;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;

public class GeneralActionsGameSystem extends GameSystem {

    private final GameWorld world;

    public GeneralActionsGameSystem(GameWorld world) {
        this.world = world;
    }

    @Override
    public void update(DeltaTime delta) {
        if (Keyboard.isKeyPressed(KeyboardKey.ESCAPE)) {
            world.getData().get(Game.class).dispose();
        }
    }
}
