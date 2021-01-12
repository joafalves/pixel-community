/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.sprite;

import org.pixel.common.DemoGame;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.Game;
import org.pixel.core.GameSettings;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

public class SingleSpriteDemo extends DemoGame {

    private ContentManager content;
    private SpriteBatch spriteBatch;

    private Texture spriteTex;
    private Vector2 spritePos;
    private Vector2 spriteAnchor;

    public SingleSpriteDemo(GameSettings settings) {
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
        spriteTex = content.load("images/earth-48x48.png", Texture.class);

        // related org.pixel.sprite properties
        spriteAnchor = Vector2.half();
        spritePos = new Vector2(getVirtualWidth() / 2f, getVirtualHeight() / 2f);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void draw(float delta) {
        // begin the spritebatch phase:
        spriteBatch.begin(gameCamera.getViewMatrix(), BlendMode.NORMAL_BLEND);

        // org.pixel.sprite definition for this drawing phase:
        spriteBatch.draw(spriteTex, spritePos, Color.WHITE, spriteAnchor, 3f);

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

        Game game = new SingleSpriteDemo(settings);
        game.start();
    }
}