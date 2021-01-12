/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.learning.text;

import org.pixel.learning.common.DemoGame;
import org.pixel.content.ContentManager;
import org.pixel.content.Font;
import org.pixel.core.Game;
import org.pixel.core.GameSettings;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

public class TextDemo extends DemoGame {

    private static final String TEXT = "The quick brown fox jumps over the lazy dog\nABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private ContentManager content;
    private SpriteBatch spriteBatch;

    private Font font;
    private Vector2 textPos;
    private Vector2 smallTextPos;

    public TextDemo(GameSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        // game related changes & definitions
        gameCamera.setOrigin(Vector2.zero());

        // general game instances
        content = new ContentManager();
        spriteBatch = new SpriteBatch();

        // load font into memory
        font = content.load("fonts/gidole-regular.ttf", Font.class);
        font.setFontSize(32);

        // related properties
        textPos = new Vector2(10, 10);
        smallTextPos = new Vector2(10, 100);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void draw(float delta) {
        // begin the spritebatch phase:
        spriteBatch.begin(gameCamera.getViewMatrix(), BlendMode.NORMAL_BLEND);

        // font definition for this drawing phase:
        spriteBatch.drawText(font, TEXT, textPos, Color.WHITE);

        // font definition for this drawing phase (downscaled):
        spriteBatch.drawText(font, TEXT, smallTextPos, Color.GOLD, 18);

        // end and draw all sprites stored:
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        content.dispose();
        spriteBatch.dispose();
        font.dispose();
    }

    public static void main(String[] args) {
        GameSettings settings = new GameSettings(600, 480);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);

        Game game = new TextDemo(settings);
        game.start();
    }
}