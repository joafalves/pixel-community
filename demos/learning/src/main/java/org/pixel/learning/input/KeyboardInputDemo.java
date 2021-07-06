/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.learning.input;

import org.pixel.commons.DeltaTime;
import org.pixel.learning.common.DemoGame;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.input.keyboard.Keyboard;
import org.pixel.input.keyboard.KeyboardKey;
import org.pixel.input.keyboard.KeyboardState;
import org.pixel.math.Vector2;

public class KeyboardInputDemo extends DemoGame {

    private static final float MOVEMENT_SPEED = 100f;

    private ContentManager content;
    private SpriteBatch spriteBatch;

    private Texture spriteTex;
    private Vector2 spritePos;
    private Vector2 spriteAnchor;

    private KeyboardState lastKeyboardState;

    public KeyboardInputDemo(WindowSettings settings) {
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

        // Keyboard direct state access for org.pixel.input detection:
        if (Keyboard.isKeyDown(KeyboardKey.UP) || Keyboard.isKeyDown(KeyboardKey.W)) {
            gameCamera.translate(0,-MOVEMENT_SPEED * delta.getElapsed()); // translate camera vertically

        } else if (Keyboard.isKeyDown(KeyboardKey.DOWN) || Keyboard.isKeyDown(KeyboardKey.S)) {
            gameCamera.translate(0,MOVEMENT_SPEED * delta.getElapsed()); // translate camera vertically
        }

        // In addition to direct state access, the Keyboard instance also supports snapshots by calling
        // 'Keyboard.getState()'. Using snapshots can be useful for comparison in-between frames (for eg. key presses)
        if (lastKeyboardState != null && Keyboard.isKeyDown(KeyboardKey.R) && lastKeyboardState.isKeyUp(KeyboardKey.R)) {
            // the key 'R' was pressed on this frame, was previously up!
            gameCamera.setPosition(0, 0); // reset camera position
        }

        // Take into consideration that the 'getState()' causes volatile memory which can result in quicker GC cycles
        lastKeyboardState = Keyboard.getState();
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
        WindowSettings settings = new WindowSettings(600, 480);
        settings.setWindowTitle("Input DEMO - use W or S keys to move the camera vertically");
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);

        PixelWindow window = new KeyboardInputDemo(settings);
        window.start();
    }
}
