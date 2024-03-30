/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.audio;

import org.pixel.audio.AudioEngine;
import org.pixel.content.ContentManager;
import org.pixel.content.ContentManagerFactory;
import org.pixel.content.Sound;
import org.pixel.demo.learning.common.DemoGame;
import org.pixel.graphics.GameWindowSettings;

public class AudioDemo extends DemoGame {

    protected Sound sound;
    protected ContentManager content;

    public AudioDemo(GameWindowSettings settings) {
        super(settings);
    }

    @Override
    public void load() {
        // general game instances
        content = ContentManagerFactory.create();

        // load the audio source into memory
        sound = content.load("audio/sfx_step_grass.ogg", Sound.class);

        // play the sound continuously
        AudioEngine.play(sound, true);
    }

    @Override
    public void dispose() {
        content.dispose();
        sound.dispose();
        super.dispose();
    }

    public static void main(String[] args) {
        var settings = new GameWindowSettings(600, 480);
        settings.setTitle("Volume up! Audio is playing :)");
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);

        var window = new AudioDemo(settings);
        window.start();
    }
}
