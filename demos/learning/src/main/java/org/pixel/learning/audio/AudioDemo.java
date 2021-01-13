/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.learning.audio;

import org.pixel.audio.AudioDevice;
import org.pixel.audio.Sound;
import org.pixel.learning.common.DemoGame;
import org.pixel.content.ContentManager;
import org.pixel.core.Game;
import org.pixel.core.GameSettings;

public class AudioDemo extends DemoGame {

    private static final float SOUND_DURATION = 0.5f;

    private ContentManager content;
    private Sound sound;
    private float soundElapsed = SOUND_DURATION;

    public AudioDemo(GameSettings settings) {
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
    public void update(float delta) {
        super.update(delta);

        soundElapsed += delta;
        if (soundElapsed > SOUND_DURATION) {
            soundElapsed = 0;
            // AudioDevice is used to play, stop and pause sound sources
            AudioDevice.play(sound);
        }
    }

    @Override
    public void draw(float delta) {
        // draw game elements here
    }

    @Override
    public void dispose() {
        content.dispose();
    }

    public static void main(String[] args) {
        GameSettings settings = new GameSettings(600, 480);
        settings.setWindowTitle("Volume up! Audio is playing :)");
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);

        Game game = new AudioDemo(settings);
        game.start();
    }
}