/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import org.lwjgl.openal.AL10;
import org.pixel.commons.lifecycle.Disposable;

public class Sound implements Disposable {

    private final int sourcePointer;

    /**
     * Constructor.
     *
     * @param sourcePointer The native pointer to the sound source.
     */
    public Sound(int sourcePointer) {
        this.sourcePointer = sourcePointer;
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
}
