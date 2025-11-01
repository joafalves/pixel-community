package org.pixel.demo.learning.ecs;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.core.Camera2D;
import org.pixel.core.Game;
import org.pixel.core.WindowSettings;
import org.pixel.ext.ecs.GameComponent;
import org.pixel.ext.ecs.GameScene;
import org.pixel.ext.ecs.Sprite;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

/**
 * Entity Component System demo.
 */
public class EcsDemo extends Game {

    private static final Logger log = LoggerFactory.getLogger(EcsDemo.class);

    private ContentManager contentManager;
    private GameScene gameScene;

    public EcsDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        var spriteBatch = SpriteBatch.create();
        contentManager = ContentManager.create();

        Sprite sprite = new Sprite("earth", contentManager.loadTexture("images/earth-48x48.png"));
        sprite.setPivot(Vector2.half());
        sprite.getTransform().setScale(3f);
        sprite.setGroup("someGroup");
        sprite.addComponent(new MovementComponent());

        gameScene = new GameScene("SampleScene", new Camera2D(this, Vector2.half()), spriteBatch);
        gameScene.addChild(sprite);

        // Example on how to use java streams to filter by group:
        var groupFilter = gameScene.getChildren().stream()
                .filter(c -> c.getGroup().equals("someGroup"))
                .toArray();
        log.debug("This scene has {0} children with group 'someGroup'.", groupFilter.length);
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

        var window = new EcsDemo(settings);
        window.start();
    }
}
