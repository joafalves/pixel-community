package org.pixel.demo.android;

import android.content.Context;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.ServiceProvider;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.Camera2D;
import org.pixel.core.Game;
import org.pixel.core.MobileGameSettings;
import org.pixel.ext.tween.Tween;
import org.pixel.ext.tween.TweenEasingMode;
import org.pixel.ext.tween.TweenLoopMode;
import org.pixel.ext.tween.TweenSequencer;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

public class AndroidDemoGame extends Game {

    private final Logger log = LoggerFactory.getLogger(AndroidDemoGame.class);

    private SpriteBatch spriteBatch;
    private ContentManager contentManager;
    private Camera2D camera;

    private Vector2 spritePositionA;
    private Vector2 spritePositionB;
    private Texture spriteTexture;
    private TweenSequencer sequencer;

    /**
     * Constructor.
     *
     * @param settings The settings to use.
     * @param context The android context.
     */
    public AndroidDemoGame(MobileGameSettings settings, Context context) {
        super(settings, context);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void load() {
        spriteBatch = ServiceProvider.create(SpriteBatch.class);
        contentManager = ServiceProvider.create(ContentManager.class);
        camera = new Camera2D(this);

        spriteTexture = contentManager.loadTexture("images/earth-48x48.png");

        spritePositionA = new Vector2(200, 200);
        Tween tweenA = new Tween(spritePositionA.getX(), spritePositionA.getY())
                .target(spritePositionA)
                .to(200, -200)
                .duration(1f)
                .loopMode(TweenLoopMode.LOOP_REVERSE)
                .easing(TweenEasingMode.SIN)
                .on((tween, state) -> log.info("Tween A: " + state));

        spritePositionB = new Vector2(-200, 200);
        Tween tweenB = new Tween(spritePositionB.getX(), spritePositionB.getY())
                .target(spritePositionB)
                .to(-200, -200)
                .duration(1f)
                .loopMode(TweenLoopMode.LOOP_REVERSE)
                .easing(TweenEasingMode.ELASTIC);

        sequencer = new TweenSequencer(tweenA, tweenB);
        sequencer.onComplete(sequencer::restart);
    }

    @Override
    public void update(DeltaTime delta) {
        sequencer.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        spriteBatch.begin(camera.getViewMatrix());
        spriteBatch.draw(spriteTexture, spritePositionA, Color.WHITE, Vector2.HALF, 4f);
        spriteBatch.draw(spriteTexture, spritePositionB, Color.WHITE, Vector2.HALF, 4f);
        spriteBatch.end();
    }

    @Override
    public void onViewportChanged(int width, int height) {
        // For the sake of the demo, we will set the virtual resolution to the same as the viewport.
        settings.setVirtualHeight(height);
        settings.setVirtualWidth(width);
        camera.setWidth(width);
        camera.setHeight(height);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        contentManager.dispose();
        super.dispose();
    }
}
