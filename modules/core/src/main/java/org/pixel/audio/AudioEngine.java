/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.audio;

import org.lwjgl.openal.AL10;
import org.pixel.content.Sound;

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
        AL10.alSourcePlay(sound.getSourcePointer());
    }

    /**
     * Set the gain of the given sound.
     *
     * @param sound The sound to set the gain of.
     * @param gain  The gain to set.
     */
    public static void setGain(Sound sound, float gain) {
        AL10.alSourcef(sound.getSourcePointer(), AL10.AL_GAIN, gain);
    }

    /**
     * Set the pitch of the given sound.
     *
     * @param sound The sound to set the pitch of.
     * @param pitch The pitch to set.
     */
    public static void setPitch(Sound sound, float pitch) {
        AL10.alSourcef(sound.getSourcePointer(), AL10.AL_PITCH, pitch);
    }

    /**
     * Set the audio panning of the given Sound instance.
     *
     * @param sound    The Sound instance to set the panning of.
     * @param panningX The x-coordinate panning value to set (-1 to 1).
     * @param panningY The y-coordinate panning value to set (-1 to 1).
     */
    public static void setPanning(Sound sound, float panningX, float panningY) {
        AL10.alSourcefv(sound.getSourcePointer(), AL10.AL_POSITION, new float[]{panningX, panningY, 0});
    }

    /**
     * Set the audio panning of the given Sound instance.
     *
     * @param sound    The Sound instance to set the panning of.
     * @param panningX The x-coordinate panning value to set (-1 to 1).
     * @param panningY The y-coordinate panning value to set (-1 to 1).
     * @param panningZ The z-coordinate panning value to set (-1 to 1).
     */
    public static void setPanning(Sound sound, float panningX, float panningY, float panningZ) {
        AL10.alSourcefv(sound.getSourcePointer(), AL10.AL_POSITION, new float[]{panningX, panningY, panningZ});
    }

    /**
     * Set the audio velocity of the given Sound instance.
     *
     * @param sound     The Sound instance to set the panning of.
     * @param velocityX The x-coordinate velocity value to set (-1 to 1).
     * @param velocityY The y-coordinate velocity value to set (-1 to 1).
     */
    public static void setVelocity(Sound sound, float velocityX, float velocityY) {
        AL10.alSourcefv(sound.getSourcePointer(), AL10.AL_VELOCITY, new float[]{velocityX, velocityY, 0});
    }

    /**
     * Set the audio velocity of the given Sound instance.
     *
     * @param sound     The Sound instance to set the panning of.
     * @param velocityX The x-coordinate velocity value to set (-1 to 1).
     * @param velocityY The y-coordinate velocity value to set (-1 to 1).
     * @param velocityZ The z-coordinate velocity value to set (-1 to 1).
     */
    public static void setVelocity(Sound sound, float velocityX, float velocityY, float velocityZ) {
        AL10.alSourcefv(sound.getSourcePointer(), AL10.AL_VELOCITY, new float[]{velocityX, velocityY, velocityZ});
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
