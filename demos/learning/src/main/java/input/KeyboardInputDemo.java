/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package input;

import common.DemoGame;
import pixel.content.ContentManager;
import pixel.content.Texture;
import pixel.core.Game;
import pixel.core.GameSettings;
import pixel.graphics.Color;
import pixel.graphics.render.BlendMode;
import pixel.graphics.render.SpriteBatch;
import pixel.input.keyboard.Keyboard;
import pixel.input.keyboard.KeyboardKey;
import pixel.input.keyboard.KeyboardState;
import pixel.math.Vector2;

public class KeyboardInputDemo extends DemoGame {

    private static final float MOVEMENT_SPEED = 100f;

    private ContentManager content;
    private SpriteBatch spriteBatch;

    private Texture spriteTex;
    private Vector2 spritePos;
    private Vector2 spriteAnchor;

    private KeyboardState lastKeyboardState;

    public KeyboardInputDemo(GameSettings settings) {
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

        // related sprite properties
        spriteAnchor = Vector2.half();
        spritePos = new Vector2(getVirtualWidth() / 2f, getVirtualHeight() / 2f);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        // Keyboard direct state access for input detection:
        if (Keyboard.isKeyDown(KeyboardKey.UP) || Keyboard.isKeyDown(KeyboardKey.W)) {
            gameCamera.translate(0,-MOVEMENT_SPEED * delta); // translate camera vertically

        } else if (Keyboard.isKeyDown(KeyboardKey.DOWN) || Keyboard.isKeyDown(KeyboardKey.S)) {
            gameCamera.translate(0,MOVEMENT_SPEED * delta); // translate camera vertically
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
    public void draw(float delta) {
        // begin the spritebatch phase:
        spriteBatch.begin(gameCamera.getViewMatrix(), BlendMode.NORMAL_BLEND);

        // sprite definition for this drawing phase:
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

        Game game = new KeyboardInputDemo(settings);
        game.start();
    }
}