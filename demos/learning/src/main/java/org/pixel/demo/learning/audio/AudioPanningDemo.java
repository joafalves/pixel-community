/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.demo.learning.audio;

import org.pixel.audio.AudioEngine;
import org.pixel.commons.DeltaTime;
import org.pixel.core.PixelWindow;
import org.pixel.core.WindowSettings;
import org.pixel.math.MathHelper;

public class AudioPanningDemo extends AudioDemo {

    private float panningValue = 0;

    public AudioPanningDemo(WindowSettings settings) {
        super(settings);
    }

    @Override
    public void update(DeltaTime delta) {
        super.update(delta);

        panningValue += 0.001f * delta.getElapsedMs(); // dummy panning reference

        AudioEngine.setPanning(sound, MathHelper.cos(panningValue));
    }

    public static void main(String[] args) {
        WindowSettings settings = new WindowSettings(600, 480);
        settings.setWindowTitle("Volume up! Audio is playing :)");
        settings.setWindowResizable(false);
        settings.setMultisampling(2);
        settings.setVsync(true);
        settings.setDebugMode(true);

        PixelWindow window = new AudioPanningDemo(settings);
        window.start();
    }
}
