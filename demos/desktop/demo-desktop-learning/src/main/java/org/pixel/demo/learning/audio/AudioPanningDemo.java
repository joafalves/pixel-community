/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.audio;

import org.pixel.audio.AudioEngine;
import org.pixel.commons.Color;
import org.pixel.commons.DeltaTime;
import org.pixel.commons.ServiceProvider;
import org.pixel.content.Texture;
import org.pixel.graphics.Camera2D;
import org.pixel.graphics.WindowSettings;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.MathHelper;
import org.pixel.math.Vector2;

public class AudioPanningDemo extends AudioDemo {

    private SpriteBatch spriteBatch;
    private Texture texture;
    private Camera2D camera;

    private float panningValue = 0;
    private Vector2 texturePosition;

    public AudioPanningDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        super.load();

        spriteBatch = ServiceProvider.create(SpriteBatch.class);
        camera = new Camera2D(this);
        texturePosition = new Vector2();

        texture = contentManager.load("images/earth-48x48.png", Texture.class);
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        panningValue += 0.001f * delta.getElapsedMs(); // dummy panning reference
        texturePosition.set(MathHelper.cos(panningValue) * 100f, MathHelper.sin(panningValue) * 100f);

        AudioEngine.setPosition(sound, MathHelper.cos(panningValue), MathHelper.sin(panningValue));
    }

    @Override
    public void draw(DeltaTime delta) {
        super.draw(delta);

        spriteBatch.begin(camera.getViewMatrix());

        spriteBatch.draw(texture, Vector2.ZERO, Color.WHITE, Vector2.HALF, 2f);

        spriteBatch.draw(texture, texturePosition, Color.WHITE, Vector2.HALF);

        spriteBatch.end();
    }

    public static void main(String[] args) {
        var settings = new WindowSettings(600, 480);
        settings.setTitle("Volume up! Audio is playing :)");
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);

        var window = new AudioPanningDemo(settings);
        window.start();
    }
}
