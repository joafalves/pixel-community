/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.learning.sprite;

import org.pixel.commons.DeltaTime;
import org.pixel.core.PixelWindow;
import org.pixel.learning.common.DemoGame;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.GameSettings;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

public class AnimatedSpriteDemo extends DemoGame {

    private static final int ANIM_STEPS = 3;
    private static final float ANIM_DELAY = 0.2f;

    private ContentManager content;
    private SpriteBatch spriteBatch;

    private Texture spriteTex;
    private Vector2 spritePos;
    private int animStep;
    private float animDelay;

    public AnimatedSpriteDemo(GameSettings settings) {
        super(settings);
        setBackgroundColor(Color.BLACK);
    }

    @Override
    public void load() {
        // game related changes & definitions
        gameCamera.setOrigin(Vector2.zero());

        // general game instances
        content = new ContentManager();
        spriteBatch = new SpriteBatch();

        // load texture into memory
        spriteTex = content.load("images/char-anim-50x85.png", Texture.class);

        // related org.pixel.learning.sprite properties
        spritePos = new Vector2(10, 10);
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        animDelay += delta.getElapsed();
        if (animDelay > ANIM_DELAY) {
            log.debug("Animation yield exceeded %f", animDelay);

            animDelay = 0;
            animStep++;
            if (animStep > ANIM_STEPS) {
                animStep = 0;
            }
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        // begin the spritebatch phase:
        spriteBatch.begin(gameCamera.getViewMatrix(), BlendMode.NORMAL_BLEND);

        // org.pixel.learning.sprite definition for this drawing phase:
        spriteBatch.draw(spriteTex, spritePos, new Rectangle(50 * animStep, 0, 50, 85), Color.WHITE,
                Vector2.zero(), 2f, 2f, 0f);

        // end and draw all sprites stored:
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        content.dispose();
        spriteBatch.dispose();
        spriteTex.dispose();
    }

    public static void main(String[] args) {
        GameSettings settings = new GameSettings(600, 480);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);

        PixelWindow window = new AnimatedSpriteDemo(settings);
        window.start();
    }
}
