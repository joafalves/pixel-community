package org.pixel.demo.learning.ecs;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.ext.ecs.GameScene;
import org.pixel.ext.ecs.Sprite;
import org.pixel.core.WindowSettings;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

public class EcsHierarchyDemo extends DemoGame {

    private ContentManager contentManager;
    private GameScene gameScene;

    private SpriteBatch spriteBatch;

    public EcsHierarchyDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        spriteBatch = ServiceProvider.get(SpriteBatch.class);
        contentManager = ServiceProvider.get(ContentManager.class);

        var parent = new Sprite("parent", contentManager.loadTexture("images/earth-48x48.png"));
        parent.setPivot(Vector2.half());
        parent.getTransform().setScale(3f);
        parent.addComponent(new RotateComponent());

        var child = new Sprite("child", contentManager.loadTexture("images/earth-48x48.png"));
        child.setPivot(Vector2.half());
        child.getTransform().setPosition(140, 0);
        child.getTransform().setScale(0.5f);

        var subChild = new Sprite("child", contentManager.loadTexture("images/earth-48x48.png"));
        subChild.setPivot(Vector2.half());
        subChild.getTransform().setPosition(80, 0);
        subChild.getTransform().setScale(0.5f);

        gameScene = new GameScene("SampleScene", gameCamera, spriteBatch);
        gameScene.addChild(parent);
        parent.addChild(child);
        child.addChild(subChild);
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
        gameScene.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);
        gameScene.draw(delta);
    }

    @Override
    public void dispose() {
        contentManager.dispose();
        spriteBatch.dispose();
        super.dispose();
    }

    private static class RotateComponent extends GameComponent {

        @Override
        public void update(DeltaTime delta) {
            float totalElapsed = delta.getTotalElapsed();
            getTransform().setRotation(totalElapsed);
        }

    }

    public static void main(String[] args) {
        var settings = new WindowSettings(600, 480);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(false);
        settings.setTargetFps(60);
        settings.setDevMode(false);

        var window = new EcsHierarchyDemo(settings);
        window.start();
    }
}
