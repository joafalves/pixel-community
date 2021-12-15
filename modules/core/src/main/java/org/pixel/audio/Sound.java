/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.audio;

import org.lwjgl.openal.AL10;
import org.pixel.commons.lifecycle.Disposable;

public class Sound implements Disposable {

    private final int sourcePointer;
    private float gain;
    private float pitch;

    /**
     * Constructor.
     *
     * @param sourcePointer The native pointer to the sound source.
     */
    public Sound(int sourcePointer) {
        this.sourcePointer = sourcePointer;
        this.gain = 1.0f;
        this.pitch = 1.0f;
    }

    /**
     * Get the native sound source pointer.
     *
     * @return The native sound source pointer.
     */
    public int getSourcePointer() {
        return this.sourcePointer;
    }

    /**
     * Dispose the sound instance and free the native resources.
     */
    @Override
    public void dispose() {
        AL10.alDeleteSources(sourcePointer);
    }

    /**
     * Get the gain of the sound.
     *
     * @return The gain of the sound.
     */
    public float getGain() {
        return gain;
    }

    /**
     * Set the gain of the sound.
     *
     * @param gain The gain of the sound.
     */
    public void setGain(float gain) {
        this.gain = gain;
    }

    /**
     * Get the pitch of the sound.
     *
     * @return The pitch of the sound.
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Set the pitch of the sound.
     *
     * @param pitch The pitch of the sound.
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
