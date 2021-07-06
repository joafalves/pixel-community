/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.learning.text;

import org.pixel.commons.DeltaTime;
import org.pixel.core.PixelWindow;
import org.pixel.learning.common.DemoGame;
import org.pixel.content.ContentManager;
import org.pixel.content.Font;
import org.pixel.core.WindowSettings;
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

    public TextDemo(WindowSettings settings) {
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
    public void update(DeltaTime delta) {
        super.update(delta);
    }

    @Override
    public void draw(DeltaTime delta) {
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
        WindowSettings settings = new WindowSettings(600, 480);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);

        PixelWindow window = new TextDemo(settings);
        window.start();
    }
}
