/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package sprite;

import common.DemoGame;
import pixel.content.ContentManager;
import pixel.content.Texture;
import pixel.core.Game;
import pixel.core.GameSettings;
import pixel.graphics.Color;
import pixel.graphics.render.BlendMode;
import pixel.graphics.render.SpriteBatch;
import pixel.math.Rectangle;
import pixel.math.Vector2;

public class AnimatedSpriteDemo extends DemoGame {

    private static final int ANIM_STEPS = 3;
    private static final float ANIM_DELAY = 0.2f;

    private ContentManager content;
    private SpriteBatch spriteBatch;

    private Texture spriteTex;
    private Vector2 spritePos;
    private int animStep;
    private float animDelay;

    public AnimatedSpriteDemo(GameSettings settings) {
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
        spriteTex = content.load("images/char-anim-50x85.png", Texture.class);

        // related sprite properties
        spritePos = new Vector2(10, 10);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        animDelay += delta;
        if (animDelay > ANIM_DELAY) {
            log.debug("Animation yield exceeded %f", animDelay);

            animDelay = 0;
            animStep++;
            if (animStep > ANIM_STEPS) {
                animStep = 0;
            }
        }
    }

    @Override
    public void draw(float delta) {
        // begin the spritebatch phase:
        spriteBatch.begin(gameCamera.getViewMatrix(), BlendMode.NORMAL_BLEND);

        // sprite definition for this drawing phase:
        spriteBatch.draw(spriteTex, spritePos, new Rectangle(50 * animStep, 0, 50, 85), Color.WHITE,
                Vector2.zero(), 2f, 2f, 0f);

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

        Game game = new AnimatedSpriteDemo(settings);
        game.start();
    }
}