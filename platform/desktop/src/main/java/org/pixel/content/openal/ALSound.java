package org.pixel.content.openal;

import lombok.Getter;
import org.lwjgl.openal.AL10;
import org.pixel.content.Sound;

@Getter
public class ALSound extends Sound {

    private final int sourcePointer;

    /**
     * Constructor
     *
     * @param sourcePointer The source pointer
     */
    public ALSound(int sourcePointer) {
        this.sourcePointer = sourcePointer;
    }

    @Override
    public void dispose() {
        if (this.getSourcePointer() >= 0) {
            AL10.alDeleteSources(this.getSourcePointer());
        }
    }
}
