package org.pixel.demo.concept.bluegem;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.core.Camera2D;
import org.pixel.core.DesktopLauncher;
import org.pixel.core.GameApp;
import org.pixel.core.WindowSettings;
import org.pixel.demo.concept.bluegem.scene.TitleGameScene;
import org.pixel.ext.ecs.SceneManager;
import org.pixel.graphics.render.SpriteBatch;

import static org.pixel.demo.concept.bluegem.BlueGemConstants.VIRTUAL_HEIGHT;
import static org.pixel.demo.concept.bluegem.BlueGemConstants.VIRTUAL_WIDTH;

public class BlueGemGame extends GameApp {

    private SpriteBatch spriteBatch;
    private SceneManager sceneManager;

    @Override
    public void load() {
        // explicit wiring (this used to be assembled by blueprints)
        spriteBatch = graphics().createSpriteBatch();
        var mainCamera = new Camera2D(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        var titleScene = new TitleGameScene("TitleScene", mainCamera, spriteBatch, content());
        sceneManager = new SceneManager(titleScene);
    }

    @Override
    public void update(DeltaTime delta) {
        sceneManager.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        sceneManager.draw(delta);
    }

    @Override
    public void dispose() {
        sceneManager.dispose();
        spriteBatch.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        settings.setTitle("BlueGem");
        settings.setVsync(true);
        settings.setBackgroundColor(new Color(0xd1dbd3ff));

        DesktopLauncher.run(new BlueGemGame(), settings);
    }
}
