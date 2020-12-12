/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.audio;

import org.lwjgl.openal.AL10;
import pixel.commons.lifecycle.Disposable;

public class Sound implements Disposable {

    private final int sourcePointer;
    private float gain;
    private float pitch;

    /**
     * Constructor
     *
     * @param sourcePointer
     */
    public Sound(int sourcePointer) {
        this.sourcePointer = sourcePointer;
        this.gain = 1.0f;
        this.pitch = 1.0f;
    }

    /**
     * Get the sound source pointer
     *
     * @return
     */
    public int getSourcePointer() {
        return this.sourcePointer;
    }

    @Override
    public void dispose() {
        AL10.alDeleteSources(sourcePointer);
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}
