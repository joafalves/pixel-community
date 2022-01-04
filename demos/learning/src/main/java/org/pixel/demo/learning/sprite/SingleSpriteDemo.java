/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.sprite;

import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

public class SingleSpriteDemo extends DemoGame {

    private ContentManager content;
    private SpriteBatch spriteBatch;
    private Texture spriteTex;

    public SingleSpriteDemo(WindowSettings settings) {
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
        spriteTex = content.load("images/screenshot-600x320.png", Texture.class);
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
        // begin the spritebatch phase:
        spriteBatch.begin(gameCamera.getViewMatrix(), BlendMode.NORMAL_BLEND);

        // org.pixel.learning.sprite definition for this drawing phase:
        spriteBatch.draw(spriteTex, Vector2.ZERO, Color.WHITE);

        // end and draw all sprites stored:
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        super.dispose();
        content.dispose();
        spriteBatch.dispose();
        spriteTex.dispose();
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(600, 320);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);

        PixelWindow window = new SingleSpriteDemo(settings);
        window.start();
    }
}
