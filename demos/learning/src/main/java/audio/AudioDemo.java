/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package audio;

import common.DemoGame;
import pixel.audio.AudioDevice;
import pixel.audio.Sound;
import pixel.content.ContentManager;
import pixel.core.Game;
import pixel.core.GameSettings;

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

        // load audio into memory
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

        Game game = new AudioDemo(settings);
        game.start();
    }
}