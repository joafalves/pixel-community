package org.pixel.content.openal;

import lombok.Getter;
import org.lwjgl.openal.AL10;
import org.pixel.content.Sound;

@Getter
public class ALSound extends Sound {

    private final int sourcePointer;
    private final int bufferPointer;
    private boolean disposed;

    /**
     * Constructor
     *
     * @param sourcePointer The source pointer
     * @deprecated the OpenAL buffer is not owned (and therefore never released) with this constructor; use
     * {@link #ALSound(int, int)}.
     */
    @Deprecated
    public ALSound(int sourcePointer) {
        this(sourcePointer, 0);
    }

    /**
     * Constructor
     *
     * @param sourcePointer The source pointer
     * @param bufferPointer The buffer holding the audio data, owned (and released) by this sound; 0 if none.
     */
    public ALSound(int sourcePointer, int bufferPointer) {
        this.sourcePointer = sourcePointer;
        this.bufferPointer = bufferPointer;
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;

        // the source must be deleted first: a buffer still attached to a source cannot be deleted
        if (this.sourcePointer > 0) {
            AL10.alDeleteSources(this.sourcePointer);
        }
        if (this.bufferPointer > 0) {
            AL10.alDeleteBuffers(this.bufferPointer);
        }
    }
}
