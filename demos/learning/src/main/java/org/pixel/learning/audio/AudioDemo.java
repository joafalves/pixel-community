/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.learning.audio;

import org.pixel.audio.AudioDevice;
import org.pixel.audio.Sound;
import org.pixel.commons.DeltaTime;
import org.pixel.core.PixelWindow;
import org.pixel.learning.common.DemoGame;
import org.pixel.content.ContentManager;
import org.pixel.core.WindowSettings;

public class AudioDemo extends DemoGame {

    private static final float SOUND_DURATION = 0.5f;

    private ContentManager content;
    private Sound sound;
    private float soundElapsed = SOUND_DURATION;

    public AudioDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        // general game instances
        content = new ContentManager();

        // load org.pixel.audio into memory
        sound = content.load("audio/sfx_step_grass.ogg", Sound.class);
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        soundElapsed += delta.getElapsed();
        if (soundElapsed > SOUND_DURATION) {
            soundElapsed = 0;
            // AudioDevice is used to play, stop and pause sound sources
            AudioDevice.play(sound);
        }
    }

    @Override
    public void draw(DeltaTime delta) {
        // draw game elements here
    }

    @Override
    public void dispose() {
        content.dispose();
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(600, 480);
        settings.setWindowTitle("Volume up! Audio is playing :)");
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);

        PixelWindow window = new AudioDemo(settings);
        window.start();
    }
}
