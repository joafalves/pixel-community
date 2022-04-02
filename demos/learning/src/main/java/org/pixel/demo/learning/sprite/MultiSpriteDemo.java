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

public class MultiSpriteDemo extends DemoGame {

    private ContentManager content;
    private SpriteBatch spriteBatch;

    private Vector2 spriteAnchor;

    private Texture spriteTexA;
    private Vector2 spritePosA;

    private Texture spriteTexB;
    private Vector2 spritePosB;

    public MultiSpriteDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        // game related changes & definitions
        gameCamera.setOrigin(Vector2.zero());

        // general game instances
        content = new ContentManager();
        spriteBatch = new SpriteBatch();

        // load texture into memory
        spriteTexA = content.load("images/earth-48x48.png", Texture.class);
        spriteTexB = content.load("images/char-anim-50x85.png", Texture.class);

        // related org.pixel.learning.sprite properties
        spriteAnchor = Vector2.half();
        spritePosA = new Vector2(getVirtualWidth() / 3f, getVirtualHeight() / 2f);
        spritePosB = new Vector2(getVirtualWidth() / 3f * 2f, getVirtualHeight() / 2f);
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
        spriteBatch.draw(spriteTexA, spritePosA, Color.WHITE, spriteAnchor, 2f);
        spriteBatch.draw(spriteTexB, spritePosB, Color.WHITE, spriteAnchor, 1f);

        // end and draw all sprites stored:
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        content.dispose();
        spriteBatch.dispose();
        spriteTexA.dispose();

        super.dispose();
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(600, 480);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(false);

        PixelWindow window = new MultiSpriteDemo(settings);
        window.start();
    }
}
