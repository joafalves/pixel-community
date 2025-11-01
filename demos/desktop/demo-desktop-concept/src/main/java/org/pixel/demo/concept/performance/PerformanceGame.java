/*
 * This software is available under Apache License
 * Copyright (c)
 */

package org.pixel.demo.concept.performance;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.service.ServiceProvider;
import org.pixel.commons.Timer;
import org.pixel.commons.logger.ConsoleLogger;
import org.pixel.commons.logger.LogLevel;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.demo.concept.performance.component.ConstantVelocityBoundComponent;
import org.pixel.ext.ecs.GameScene;
import org.pixel.ext.ecs.Sprite;
import org.pixel.ext.ecs.component.ConstantRotationComponent;
import org.pixel.core.Camera2D;
import org.pixel.core.WindowSettings;
import org.pixel.core.Game;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.Boundary;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

public class PerformanceGame extends Game {

    private static final Logger log = LoggerFactory.getLogger(PerformanceGame.class);

    private static final int SPRITE_COUNT = 4096;
    private static final float SPRITE_MOVEMENT_SPEED = 100f;
    private static final boolean MULTI_TEXTURE = true;

    private ContentManager contentManager;
    private GameScene gameScene;
    private SpriteBatch spriteBatch;
    private Camera2D camera;
    private Boundary screenBoundary;

    private final Timer debugTimer = new Timer(1000);

    public PerformanceGame(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        spriteBatch = SpriteBatch.create();
        contentManager = ContentManager.create();
        camera = new Camera2D(this, Vector2.zero());
        gameScene = new GameScene("GameScene01", camera, spriteBatch);

        screenBoundary = new Boundary(0, 0, getViewportWidth(), getViewportHeight());
        var textureArray = new Texture[]{
                contentManager.loadTexture("images/circle.png"),
                contentManager.loadTexture("images/triangle.png"),
                contentManager.loadTexture("images/rounded_square.png"),
                contentManager.loadTexture("images/heart.png"),
                contentManager.loadTexture("images/star.png"),
        };
        for (int i = 0; i < SPRITE_COUNT; i++) {
            var velocity = new Vector2(MathHelper.random(-SPRITE_MOVEMENT_SPEED, SPRITE_MOVEMENT_SPEED),
                    MathHelper.random(-SPRITE_MOVEMENT_SPEED, SPRITE_MOVEMENT_SPEED));
            var sprite = new Sprite("Sprite_" + i, MULTI_TEXTURE ? textureArray[i % textureArray.length] : textureArray[0]);
            sprite.setOverlayColor(Color.random());
            sprite.getTransform().setPosition(
                    MathHelper.random(0, getViewportWidth()), MathHelper.random(0, getViewportHeight()));
            sprite.addComponent(
                    new ConstantVelocityBoundComponent(velocity, screenBoundary));
            sprite.addComponent(
                    new ConstantRotationComponent(MathHelper.random(-5f, 5f)));
            sprite.setPivot(Vector2.half());

            gameScene.addChild(sprite);
        }

        log.info("Drawing {0} sprites.", SPRITE_COUNT);
    }

    @Override
    public void update(DeltaTime delta) {
        gameScene.update(delta);

        if (debugTimer.elapsed()) {
            log.info("FPS: {0} - Smoothed FPS: {1}.", getFps(), getSmoothedFps());
        }

        if (Keyboard.isKeyPressed(KeyboardKey.ESCAPE)) {
            quit();
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        gameScene.draw(delta);
    }

    @Override
    public void onWindowSizeChange(int width, int height) {
        super.onWindowSizeChange(width, height);
        syncViewportSize();
        camera.setSize(width, height);
        screenBoundary.set(0, 0 , width, height);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        contentManager.dispose();
        gameScene.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings("Performance", 1280, 720);
        settings.setVsync(false);
        settings.setIdleThrottle(false);
        settings.setMultisampling(4);
        settings.setWindowResizable(true);

        ConsoleLogger.setLogLevel(LogLevel.TRACE);

        var game = new PerformanceGame(settings);
        game.start();
    }
}
