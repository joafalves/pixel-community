/*
 * This software is available under Apache License
 * Copyright (c)
 */

package org.pixel.demo.concept.performance;

import org.pixel.commons.DeltaTime;
import org.pixel.commons.logger.ConsoleLogger;
import org.pixel.commons.logger.LogLevel;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.Camera2D;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.demo.concept.commons.TitleFpsCounter;
import org.pixel.demo.concept.performance.component.ConstantVelocityBoundComponent;
import org.pixel.ext.ecs.GameScene;
import org.pixel.ext.ecs.Sprite;
import org.pixel.ext.ecs.component.ConstantRotationComponent;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

public class PerformanceGame extends PixelWindow {

    private static final int SPRITE_COUNT = 1024;
    private static final float SPRITE_MOVEMENT_SPEED = 100f;
    private static final boolean ALTERNATE_TEXTURE = true;

    private ContentManager contentManager;
    private TitleFpsCounter fpsCounter;
    private GameScene gameScene;
    private SpriteBatch spriteBatch;

    public PerformanceGame(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        fpsCounter = new TitleFpsCounter(this);
        contentManager = new ContentManager();
        spriteBatch = new SpriteBatch();
        gameScene = new GameScene("GameScene01", new Camera2D(this, Vector2.zero()), spriteBatch);

        var screenBoundary = new Boundary(0, 0, getVirtualWidth(), getVirtualHeight());
        var textureArray = new Texture[] {
                contentManager.loadTexture("images/square.png"),
                contentManager.loadTexture("images/circle.png"),
                contentManager.loadTexture("images/triangle.png")
        };
        for (int i = 0; i < SPRITE_COUNT; i++) {
            var velocity = new Vector2(MathHelper.random(-SPRITE_MOVEMENT_SPEED, SPRITE_MOVEMENT_SPEED),
                    MathHelper.random(-SPRITE_MOVEMENT_SPEED, SPRITE_MOVEMENT_SPEED));
            var sprite = new Sprite("Sprite_" + i, ALTERNATE_TEXTURE ? textureArray[i % textureArray.length] : textureArray[0]);
            sprite.setOverlayColor(ALTERNATE_TEXTURE ? Color.random() : Color.WHITE);
            sprite.getTransform().setPosition(
                    MathHelper.random(0, getVirtualWidth()), MathHelper.random(0, getVirtualHeight()));
            sprite.addComponent(
                    new ConstantVelocityBoundComponent(velocity, screenBoundary));
            sprite.addComponent(
                    new ConstantRotationComponent(MathHelper.random(1f, 4f)));

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
        WindowSettings settings = new WindowSettings("Performance", 1280, 720);
        settings.setVsync(false);
        settings.setIdleThrottle(false);
        settings.setWindowResizable(true);

        ConsoleLogger.setLogLevel(LogLevel.TRACE);

        PerformanceGame game = new PerformanceGame(settings);
        game.start();
    }
}
