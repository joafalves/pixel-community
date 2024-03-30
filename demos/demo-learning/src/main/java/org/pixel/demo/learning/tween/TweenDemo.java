package org.pixel.demo.learning.tween;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.content.ContentManagerFactory;
import org.pixel.content.Texture;
import org.pixel.ext.tween.Tween;
import org.pixel.ext.tween.TweenEasingMode;
import org.pixel.ext.tween.TweenLoopMode;
import org.pixel.graphics.Camera2D;
import org.pixel.graphics.GameWindowSettings;
import org.pixel.graphics.GameWindow;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.graphics.render.SpriteBatchFactory;
import org.pixel.math.Vector2;

public class TweenDemo extends GameWindow {

    private final Logger log = LoggerFactory.getLogger(TweenDemo.class);

    private SpriteBatch spriteBatch;
    private ContentManager contentManager;
    private Camera2D camera;

    private Vector2 spritePositionA;
    private Vector2 spritePositionB;
    private Texture spriteTexture;
    private Tween tweenA;
    private Tween tweenB;

    public TweenDemo(GameWindowSettings settings) {
        super(settings);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void load() {
        contentManager = ContentManagerFactory.create();
        spriteBatch = SpriteBatchFactory.create(this);
        camera = new Camera2D(this);

        spriteTexture = contentManager.loadTexture("images/earth-48x48.png");

        spritePositionA = new Vector2();
        tweenA = new Tween(200, 200)
                .to(200, -200)
                .duration(1f)
                .loopMode(TweenLoopMode.LOOP_REVERSE)
                .easing(TweenEasingMode.SIN)
                .on((tween, state) -> log.info("Tween A: " + state));

        spritePositionB = new Vector2();
        tweenB = new Tween(-200, 200)
                .to(-200, -200)
                .duration(1f)
                .loopMode(TweenLoopMode.LOOP_REVERSE)
                .easing(TweenEasingMode.ELASTIC);
    }

    @Override
    public void update(DeltaTime delta) {
        tweenA.update(delta, spritePositionA);
        tweenB.update(delta, spritePositionB);
    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.begin(camera.getViewMatrix());
        spriteBatch.draw(spriteTexture, spritePositionA, Color.WHITE, Vector2.HALF, 3f);
        spriteBatch.draw(spriteTexture, spritePositionB, Color.WHITE, Vector2.HALF, 3f);
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        contentManager.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new GameWindowSettings(800, 600);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(false);
        settings.setIdleThrottle(false);

        var window = new TweenDemo(settings);
        window.start();
    }
}
