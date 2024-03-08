/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import org.pixel.commons.lifecycle.Disposable;

public abstract class Sound implements Disposable {

    protected int sourcePointer;

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
}
