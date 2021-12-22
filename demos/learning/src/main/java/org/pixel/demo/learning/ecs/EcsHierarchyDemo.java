package org.pixel.demo.learning.ecs;

import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.core.Camera2D;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.ext.ecs.GameScene;
import org.pixel.ext.ecs.Sprite;

public class EcsHierarchyDemo extends PixelWindow {

    private ContentManager contentManager;
    private Camera2D gameCamera;
    private GameScene gameScene;

    public EcsHierarchyDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        contentManager = new ContentManager();
        gameCamera = new Camera2D(this);

        Sprite parent = new Sprite("parent", contentManager.loadTexture("images/earth-48x48.png"));
        parent.getTransform().setScale(3f);
        parent.addComponent(new RotateComponent());

        Sprite child = new Sprite("child", contentManager.loadTexture("images/earth-48x48.png"));
        child.getTransform().setPosition(140, 0);
        child.getTransform().setScale(0.5f);

        Sprite subChild = new Sprite("child", contentManager.loadTexture("images/earth-48x48.png"));
        subChild.getTransform().setPosition(80, 0);
        subChild.getTransform().setScale(0.5f);

        gameScene = new GameScene("SampleScene", gameCamera);
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

    private static class RotateComponent extends GameComponent {

        @Override
        public void update(DeltaTime delta) {
            float totalElapsed = delta.getTotalElapsed();
            getTransform().setRotation(totalElapsed);
        }

    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(600, 480);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(false);

        PixelWindow window = new EcsHierarchyDemo(settings);
        window.start();
    }
}
