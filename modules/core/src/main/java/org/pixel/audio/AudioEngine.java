/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.audio;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.pixel.content.Sound;

public class AudioEngine {

    private static final FloatBuffer readBufferFloat = BufferUtils.createFloatBuffer(1);

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
     * Get the current gain of the given sound.
     *
     * @param sound The sound to get the gain of.
     * @return The current gain of the given sound.
     */
    public static synchronized float getGain(Sound sound) {
        AL10.alGetSourcef(sound.getSourcePointer(), AL10.AL_GAIN, readBufferFloat);
        return readBufferFloat.get(0);
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
     * Get the current pitch of the given sound.
     *
     * @param sound The sound to get the pitch of.
     * @return The current pitch of the given sound.
     */
    public static synchronized float getPitch(Sound sound) {
        AL10.alGetSourcef(sound.getSourcePointer(), AL10.AL_PITCH, readBufferFloat);
        return readBufferFloat.get(0);
    }

    /**
     * Set the position of the given Sound instance.
     *
     * @param sound     The Sound instance to set the position of.
     * @param positionX The x-coordinate position value to set (-1 to 1).
     * @param positionY The y-coordinate position value to set (-1 to 1).
     */
    public static void setPosition(Sound sound, float positionX, float positionY) {
        AL10.alSourcefv(sound.getSourcePointer(), AL10.AL_POSITION, new float[]{positionX, positionY, 0});
    }

    /**
     * Set the position of the given Sound instance.
     *
     * @param sound     The Sound instance to set the position of.
     * @param positionX The x-coordinate position value to set (-1 to 1).
     * @param positionY The y-coordinate position value to set (-1 to 1).
     * @param positionZ The z-coordinate position value to set (-1 to 1).
     */
    public static void setPosition(Sound sound, float positionX, float positionY, float positionZ) {
        AL10.alSourcefv(sound.getSourcePointer(), AL10.AL_POSITION, new float[]{positionX, positionY, positionZ});
    }

    /**
     * Set the velocity of the given Sound instance.
     *
     * @param sound     The Sound instance to set the velocity of.
     * @param velocityX The x-coordinate velocity value to set (-1 to 1).
     * @param velocityY The y-coordinate velocity value to set (-1 to 1).
     */
    public static void setVelocity(Sound sound, float velocityX, float velocityY) {
        AL10.alSourcefv(sound.getSourcePointer(), AL10.AL_VELOCITY, new float[]{velocityX, velocityY, 0});
    }

    /**
     * Set the velocity of the given Sound instance.
     *
     * @param sound     The Sound instance to set the velocity of.
     * @param velocityX The x-coordinate velocity value to set (-1 to 1).
     * @param velocityY The y-coordinate velocity value to set (-1 to 1).
     * @param velocityZ The z-coordinate velocity value to set (-1 to 1).
     */
    public static void setVelocity(Sound sound, float velocityX, float velocityY, float velocityZ) {
        AL10.alSourcefv(sound.getSourcePointer(), AL10.AL_VELOCITY, new float[]{velocityX, velocityY, velocityZ});
    }

    /**
     * Set the playing time position.
     *
     * @param sound   The Sound instance to set the time position of.
     * @param seconds The time position to set (in seconds).
     */
    public static void setTimePosition(Sound sound, float seconds) {
        AL11.alSourcef(sound.getSourcePointer(), AL11.AL_SEC_OFFSET, seconds);
    }

    /**
     * Get the playing time position of the given Sound instance.
     *
     * @param sound The Sound instance to get the time position of.
     * @return The time position of the given Sound instance.
     */
    public static synchronized float getTimePosition(Sound sound) {
        AL11.alGetSourcef(sound.getSourcePointer(), AL11.AL_SEC_OFFSET, readBufferFloat);
        return readBufferFloat.get(0);
    }

}
