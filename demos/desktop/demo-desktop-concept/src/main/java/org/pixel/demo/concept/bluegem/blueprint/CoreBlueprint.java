package org.pixel.demo.concept.bluegem.blueprint;

import org.pixel.blueprint.annotation.Auto;
import org.pixel.blueprint.annotation.Blueprint;
import org.pixel.blueprint.annotation.Component;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.core.Camera2D;
import org.pixel.ext.ecs.GameScene;
import org.pixel.ext.ecs.SceneManager;
import org.pixel.graphics.render.SpriteBatch;

import static org.pixel.demo.concept.bluegem.BlueGemConstants.VIRTUAL_HEIGHT;
import static org.pixel.demo.concept.bluegem.BlueGemConstants.VIRTUAL_WIDTH;

@Blueprint
public class CoreBlueprint {

    private static final String DEFAULT_SCENE = "title.scene";

    @Component
    public SpriteBatch spriteBatch() {
        return SpriteBatch.create();
    }

    @Component
    public Camera2D mainCamera() {
        return new Camera2D(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
    }

    @Component
    public GameScene defaultGameScene(
            @Auto(DEFAULT_SCENE) GameScene defaultScene
    ) {
        return defaultScene;
    }

    @Component
    public SceneManager sceneManager(
            @Auto GameScene defaultGameScene
    ) {
        return new SceneManager(defaultGameScene);
    }

}
