/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.learning.input;

import org.pixel.commons.DeltaTime;
import org.pixel.learning.common.DemoGame;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.Game;
import org.pixel.core.GameSettings;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.input.gamepad.GamePad;
import org.pixel.input.gamepad.GamePadButton;
import org.pixel.input.gamepad.GamePadIndex;
import org.pixel.input.gamepad.GamePadState;
import org.pixel.math.Vector2;

public class GamePadInputDemo extends DemoGame {

    private static final float MOVEMENT_SPEED = 100f;

    private ContentManager content;
    private SpriteBatch spriteBatch;

    private Texture spriteTex;
    private Vector2 spritePos;
    private Vector2 spriteAnchor;

    public GamePadInputDemo(GameSettings settings) {
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

        // related org.pixel.learning.sprite properties
        spriteAnchor = Vector2.half();
        spritePos = new Vector2(getVirtualWidth() / 2f, getVirtualHeight() / 2f);
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        // attempt to grab the game pad state from player1
        GamePadState state = GamePad.getState(GamePadIndex.P1);

        if (state != null) { // is game pad data from player 1 available?
            if (state.isButtonDown(GamePadButton.DPAD_UP)) {
                gameCamera.translate(0,-MOVEMENT_SPEED * delta.getElapsed()); // translate camera vertically

            } else if (state.isButtonDown(GamePadButton.DPAD_DOWN)) {
                gameCamera.translate(0,MOVEMENT_SPEED * delta.getElapsed()); // translate camera vertically
            }
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        // begin the spritebatch phase:
        spriteBatch.begin(gameCamera.getViewMatrix(), BlendMode.NORMAL_BLEND);

        // org.pixel.learning.sprite definition for this drawing phase:
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
        settings.setWindowTitle("Input DEMO - use W or S keys to move the camera vertically");
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);

        Game game = new GamePadInputDemo(settings);
        game.start();
    }
}