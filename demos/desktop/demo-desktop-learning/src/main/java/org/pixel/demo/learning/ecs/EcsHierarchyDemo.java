package org.pixel.demo.learning.ecs;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.ext.ecs.GameScene;
import org.pixel.ext.ecs.Sprite;
import org.pixel.core.Camera2D;
import org.pixel.graphics.WindowSettings;
import org.pixel.graphics.Game;
import org.pixel.graphics.render.SpriteBatch;

public class EcsHierarchyDemo extends Game {

    private ContentManager contentManager;
    private GameScene gameScene;

    public EcsHierarchyDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        var spriteBatch = ServiceProvider.create(SpriteBatch.class);
        contentManager = ServiceProvider.create(ContentManager.class);

        var parent = new Sprite("parent", contentManager.loadTexture("images/earth-48x48.png"));
        parent.getTransform().setScale(3f);
        parent.addComponent(new RotateComponent());

        var child = new Sprite("child", contentManager.loadTexture("images/earth-48x48.png"));
        child.getTransform().setPosition(140, 0);
        child.getTransform().setScale(0.5f);

        var subChild = new Sprite("child", contentManager.loadTexture("images/earth-48x48.png"));
        subChild.getTransform().setPosition(80, 0);
        subChild.getTransform().setScale(0.5f);

        gameScene = new GameScene("SampleScene", new Camera2D(this), spriteBatch);
        gameScene.addChild(parent);
        parent.addChild(child);
        child.addChild(subChild);
    }

    @Override
    public void update(DeltaTime delta) {
        gameScene.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        gameScene.draw(delta);
    }

    @Override
    public void dispose() {
        contentManager.dispose();
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
        settings.setVsync(true);
        settings.setDebugMode(false);

        var window = new EcsHierarchyDemo(settings);
        window.start();
    }
}
