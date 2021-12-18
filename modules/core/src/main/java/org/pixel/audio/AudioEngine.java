/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.audio;

import org.lwjgl.openal.AL10;

public class AudioEngine {

    /**
     * Play the given Sound instance.
     *
     * @param sound The Sound instance to play.
     */
    public static void play(Sound sound) {
        play(sound, false);
    }

    /**
     * Play the given Sound instance.
     *
     * @param sound The Sound instance to play.
     * @param loop  Whether the sound should loop.
     */
    public static void play(Sound sound, boolean loop) {
        AL10.alSourcei(sound.getSourcePointer(), AL10.AL_LOOPING, loop ? AL10.AL_TRUE : AL10.AL_FALSE);
        AL10.alSourcef(sound.getSourcePointer(), AL10.AL_PITCH, sound.getPitch());
        AL10.alSourcef(sound.getSourcePointer(), AL10.AL_GAIN, sound.getGain());
        AL10.alSourcePlay(sound.getSourcePointer());
    }

    /**
     * Set the (horizontal) audio panning of the given Sound instance.
     *
     * @param sound   The Sound instance to set the panning of.
     * @param panning The panning to set (-1 to 1).
     */
    public static void setPanning(Sound sound, float panning) {
        AL10.alSourcefv(sound.getSourcePointer(), AL10.AL_POSITION, new float[]{panning, 0, 0});
    }

    /**
     * Set the audio panning of the given Sound instance.
     *
     * @param sound    The Sound instance to set the panning of.
     * @param panningH The horizontal panning to set (-1 to 1).
     * @param panningV The vertical panning to set (-1 to 1).
     */
    public static void setPanning(Sound sound, float panningH, float panningV) {
        AL10.alSourcefv(sound.getSourcePointer(), AL10.AL_POSITION, new float[]{panningH, panningV, 0});
    }

    /**
     * Pause the given Sound instance.
     *
     * @param sound The Sound instance to pause.
     */
    public static void pause(Sound sound) {
        AL10.alSourcePause(sound.getSourcePointer());
    }

    /**
     * Stop a given Sound instance.
     *
     * @param sound The Sound instance to stop.
     */
    public static void stop(Sound sound) {
        AL10.alSourceStop(sound.getSourcePointer());
    }
}
