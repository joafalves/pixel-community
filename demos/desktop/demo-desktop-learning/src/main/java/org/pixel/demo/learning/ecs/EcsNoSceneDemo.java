package org.pixel.demo.learning.ecs;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.ext.ecs.Sprite;
import org.pixel.core.WindowSettings;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

/**
 * Entity Component System demo without using the Scene system.
 */
public class EcsNoSceneDemo extends DemoGame {

    private ContentManager contentManager;

    private SpriteBatch spriteBatch;
    private Sprite sprite;

    public EcsNoSceneDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        spriteBatch = SpriteBatch.create();
        contentManager = ContentManager.create();

        sprite = new Sprite("earth", contentManager.loadTexture("images/earth-48x48.png"));
        sprite.setPivot(Vector2.half());
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
        settings.setDevMode(false);

        var window = new EcsNoSceneDemo(settings);
        window.start();
    }
}
