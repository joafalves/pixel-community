package org.pixel.demo.learning.ecs;

import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.core.Camera2D;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.ext.ecs.Sprite;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.MathHelper;

/**
 * Entity Component System demo without using the Scene system.
 */
public class EcsNoSceneDemo extends PixelWindow {

    private ContentManager contentManager;
    private Camera2D gameCamera;

    private SpriteBatch spriteBatch;
    private Sprite sprite;

    public EcsNoSceneDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        contentManager = new ContentManager();
        gameCamera = new Camera2D(this);
        spriteBatch = new SpriteBatch();

        sprite = new Sprite("earth", contentManager.loadTexture("images/earth-48x48.png"));
        sprite.getTransform().setScale(3f);
        sprite.addComponent(new MovementComponent());
    }

    @Override
    public void update(DeltaTime delta) {
        sprite.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.begin(gameCamera.getViewMatrix());
        sprite.draw(delta, spriteBatch);
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        contentManager.dispose();
        super.dispose();
    }

    private static class MovementComponent extends GameComponent {

        @Override
        public void update(DeltaTime delta) {
            float totalElapsed = delta.getTotalElapsed();
            getTransform().setPosition(
                    MathHelper.cos(totalElapsed) * 100f,
                    MathHelper.sin(totalElapsed) * 100f);
        }

    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(600, 480);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(false);

        PixelWindow window = new EcsNoSceneDemo(settings);
        window.start();
    }
}
