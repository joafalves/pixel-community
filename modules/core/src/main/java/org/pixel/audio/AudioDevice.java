/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.audio;

import org.lwjgl.openal.AL10;

import static org.lwjgl.openal.AL10.AL_TRUE;
import static org.lwjgl.openal.ALC10.ALC_FALSE;

/**
 * @author João Filipe Alves
 */
public class AudioDevice {

    /**
     * Play a given sound object
     *
     * @param sound
     */
    public static void play(Sound sound) {
        play(sound, false);
    }

    /**
     * Play a given sound object
     *
     * @param sound
     */
    public static void play(Sound sound, boolean loop) {
        AL10.alSourcei(sound.getSourcePointer(), AL10.AL_LOOPING, loop ? AL_TRUE : ALC_FALSE);
        AL10.alSourcef(sound.getSourcePointer(), AL10.AL_PITCH, sound.getPitch());
        AL10.alSourcef(sound.getSourcePointer(), AL10.AL_GAIN, sound.getGain());
        AL10.alSourcePlay(sound.getSourcePointer());
    }

    /**
     * Pause a given sound object
     *
     * @param sound
     */
    public static void pause(Sound sound) {
        AL10.alSourcePause(sound.getSourcePointer());
    }

    /**
     * Stop a given sound object
     *
     * @param sound
     */
    public static void stop(Sound sound) {
        AL10.alSourceStop(sound.getSourcePointer());
    }
}
