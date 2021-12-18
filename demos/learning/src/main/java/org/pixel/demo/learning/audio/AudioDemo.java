/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.audio;

import org.pixel.audio.AudioEngine;
import org.pixel.content.ContentManager;
import org.pixel.content.Sound;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.demo.learning.common.DemoGame;

public class AudioDemo extends DemoGame {

    protected Sound sound;
    protected ContentManager content;

    public AudioDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        // general game instances
        content = new ContentManager();

        // load the audio source into memory
        sound = content.load("audio/sfx_step_grass.ogg", Sound.class);

        // play the sound continuously
        AudioEngine.play(sound, true);
    }

    @Override
    public void dispose() {
        content.dispose();
        sound.dispose();
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
