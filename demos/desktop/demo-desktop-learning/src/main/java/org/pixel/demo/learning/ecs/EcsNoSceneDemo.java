package org.pixel.demo.learning.ecs;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.ext.ecs.Sprite;
import org.pixel.core.Camera2D;
import org.pixel.graphics.WindowSettings;
import org.pixel.graphics.Game;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.MathHelper;

/**
 * Entity Component System demo without using the Scene system.
 */
public class EcsNoSceneDemo extends Game {

    private ContentManager contentManager;
    private Camera2D gameCamera;

    private SpriteBatch spriteBatch;
    private Sprite sprite;

    public EcsNoSceneDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        gameCamera = new Camera2D(this);
        spriteBatch = ServiceProvider.create(SpriteBatch.class);
        contentManager = ServiceProvider.create(ContentManager.class);

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
        var settings = new WindowSettings(600, 480);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(false);

        var window = new EcsNoSceneDemo(settings);
        window.start();
    }
}
