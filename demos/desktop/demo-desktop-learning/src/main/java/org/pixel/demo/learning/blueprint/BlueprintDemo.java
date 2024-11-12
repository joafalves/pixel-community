package org.pixel.demo.learning.blueprint;

import org.pixel.blueprint.annotation.Auto;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.content.ContentManager;
import org.pixel.content.Texture;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;

public class BlueprintDemo extends DemoGame {

    @Auto("someGameWindowContentManager")
    private ContentManager content;
    @Auto
    private SpriteBatch spriteBatch;
    @Auto
    private Texture backgroundTexture;

    public BlueprintDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        // game related changes & definitions
        gameCamera.setOrigin(Vector2.zero());
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
        spriteBatch.draw(backgroundTexture, Vector2.ZERO, Color.WHITE);

        // end and draw all sprites stored:
        spriteBatch.end();
    }

    @Override
    public void dispose() {
        content.dispose();
        spriteBatch.dispose();
        backgroundTexture.dispose();

        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(600, 320);
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);
        // Note: Given packages are recursively resolved:
        settings.setBlueprintPackages(new String[]{"org.pixel.demo.learning.blueprint"});

        var window = new BlueprintDemo(settings);
        window.start();
    }
}