package org.pixel.demo.concept.bluegem;

import org.pixel.blueprint.annotation.Auto;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.core.Game;
import org.pixel.core.WindowSettings;
import org.pixel.ext.ecs.SceneManager;

import static org.pixel.demo.concept.bluegem.BlueGemConstants.VIRTUAL_HEIGHT;
import static org.pixel.demo.concept.bluegem.BlueGemConstants.VIRTUAL_WIDTH;

public class BlueGemGame extends Game {

    @Auto
    private SceneManager sceneManager;

    public BlueGemGame(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        sceneManager.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);
        sceneManager.draw(delta);
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        settings.setTitle("BlueGem");
        settings.setVsync(true);
        settings.setBackgroundColor(new Color(0xd1dbd3ff));
        // Set the base-package entries for the blueprint assembler (this is so that the assembler can find the
        // blueprint classes and the respective components):
        settings.setBlueprintPackages(new String[]{"org.pixel.demo.concept.bluegem"});

        var game = new BlueGemGame(settings);
        game.start();
    }
}
