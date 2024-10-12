/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.text;

import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.ServiceProvider;
import org.pixel.content.ContentManager;
import org.pixel.content.Font;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.core.WindowSettings;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.math.Vector2;

public class TextDemo extends DemoGame {

    private static final String BASE_TEXT = "Type something on your keyboard:\n";

    private ContentManager content;
    private SpriteBatch spriteBatch;

    private Font font;
    private String text = BASE_TEXT;
    private Vector2 textPosition;

    public TextDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        // game related changes & definitions
        gameCamera.setOrigin(Vector2.zero());

        // general game instances
        spriteBatch = ServiceProvider.create(SpriteBatch.class);
        content = ServiceProvider.create(ContentManager.class);

        // load font into memory
        font = content.load("fonts/gidole-regular.ttf", Font.class);
        font.setFontSize(32); // the base font-size (as it will be applied on the generated texture)

        textPosition = new Vector2(10, 10);

        // bind keyboard direct input to add user text:
        Keyboard.addCharListener(character -> text += character);
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        if (Keyboard.isKeyPressed(KeyboardKey.ENTER)) {
            text += "\n";

        } else if (Keyboard.isKeyPressed(KeyboardKey.BACKSPACE) && text.length() > BASE_TEXT.length()) {
            text = text.substring(0, text.length() - 1);
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        // begin the spritebatch phase:
        spriteBatch.begin(gameCamera.getViewMatrix(), BlendMode.NORMAL_BLEND);

        // font definition for this drawing phase:
        spriteBatch.drawText(font, text, textPosition, Color.WHITE, font.getFontSize());

        // end and draw all sprites stored:
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        content.dispose();
        spriteBatch.dispose();
        font.dispose();

        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(800, 600);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(false);

        var window = new TextDemo(settings);
        window.start();
    }
}
