/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.text;

import java.util.ArrayList;
import java.util.List;
import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.content.Font;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

public class TextDemo extends DemoGame {

    private static final String TEXT = "The quick brown fox jumps over the lazy dog\nABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private ContentManager content;
    private SpriteBatch spriteBatch;

    private Font font;
    private List<TextDisplayDemo> textDisplayDemoList;

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
        font.setFontSize(32); // the base font-size (as it will be applied on the generated texture)

        textDisplayDemoList = new ArrayList<>();
        for (float verticalOffset = 10; verticalOffset < getVirtualHeight(); ) {
            TextDisplayDemo textDisplayDemo = new TextDisplayDemo();
            textDisplayDemo.fontSize = verticalOffset == 10 ? 32 : MathHelper.random(16, font.getFontSize());
            textDisplayDemo.textColor = verticalOffset == 10 ? Color.WHITE : Color.random();
            textDisplayDemo.verticalOffset = verticalOffset;
            textDisplayDemoList.add(textDisplayDemo);

            verticalOffset += textDisplayDemo.fontSize * 2 + 10; // 2 lines + 5 pixels between each line
        }
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
        for (TextDisplayDemo textDisplayDemo : textDisplayDemoList) {
            spriteBatch.drawText(font, TEXT, new Vector2(10, textDisplayDemo.verticalOffset),
                    textDisplayDemo.textColor, textDisplayDemo.fontSize);
        }

        // end and draw all sprites stored:
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        content.dispose();
        spriteBatch.dispose();
        font.dispose();
    }

    private static class TextDisplayDemo {

        private int fontSize;
        private float verticalOffset;
        private Color textColor;
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(900, 420);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(false);

        PixelWindow window = new TextDemo(settings);
        window.start();
    }
}
