package org.pixel.content.openal;

import org.lwjgl.openal.AL10;
import org.pixel.content.Sound;

public class ALSound extends Sound {

    public ALSound(int sourcePointer) {
        super(sourcePointer);
    }

    @Override
    public void dispose() {
        AL10.alDeleteSources(this.getSourcePointer());
    }
    
}
