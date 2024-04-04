/*
 * This software is available under Apache License
 * Copyright (c)
 */

package org.pixel.demo.concept.performance;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.ServiceProvider;
import org.pixel.commons.logger.ConsoleLogger;
import org.pixel.commons.logger.LogLevel;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.demo.concept.commons.FpsCounter;
import org.pixel.demo.concept.performance.component.ConstantVelocityBoundComponent;
import org.pixel.ext.ecs.GameScene;
import org.pixel.ext.ecs.Sprite;
import org.pixel.ext.ecs.component.ConstantRotationComponent;
import org.pixel.graphics.Camera2D;
import org.pixel.graphics.GameWindowSettings;
import org.pixel.graphics.GameWindow;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

public class PerformanceGame extends GameWindow {

    private static final int SPRITE_COUNT = 5000;
    private static final float SPRITE_MOVEMENT_SPEED = 100f;
    private static final boolean MULTI_TEXTURE = true;

    private ContentManager contentManager;
    private FpsCounter fpsCounter;
    private GameScene gameScene;
    private SpriteBatch spriteBatch;

    public PerformanceGame(GameWindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        fpsCounter = new FpsCounter(this);
        spriteBatch = ServiceProvider.create(SpriteBatch.class);
        contentManager = ServiceProvider.create(ContentManager.class);
        gameScene = new GameScene("GameScene01", new Camera2D(this, Vector2.zero()), spriteBatch);

        var screenBoundary = new Boundary(0, 0, getVirtualWidth(), getVirtualHeight());
        var textureArray = new Texture[] {
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
                    MathHelper.random(0, getVirtualWidth()), MathHelper.random(0, getVirtualHeight()));
            sprite.addComponent(
                    new ConstantVelocityBoundComponent(velocity, screenBoundary));
            sprite.addComponent(
                    new ConstantRotationComponent(MathHelper.random(-5f, 5f)));

            gameScene.addChild(sprite);
        }
    }

    @Override
    public void update(DeltaTime delta) {
        fpsCounter.update(delta);
        gameScene.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        gameScene.draw(delta);
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        contentManager.dispose();
        gameScene.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new GameWindowSettings("Performance", 1280, 720);
        settings.setVsync(false);
        settings.setIdleThrottle(false);
        settings.setWindowResizable(true);

        ConsoleLogger.setLogLevel(LogLevel.TRACE);

        var game = new PerformanceGame(settings);
        game.start();
    }
}
