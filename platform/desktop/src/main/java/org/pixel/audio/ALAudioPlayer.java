package org.pixel.audio;

import org.lwjgl.openal.AL11;
import org.pixel.content.Sound;
import org.pixel.content.openal.ALSound;

public class ALAudioPlayer implements AudioPlayer {

    @Override
    public void play(Sound sound) {
        play(sound, false);
    }

    @Override
    public void play(Sound sound, boolean loop) {
        sync(sound);                                                                    // sync the sound properties

        int source = getSourcePointer(sound);
        AL11.alSourcef(source, AL11.AL_SEC_OFFSET, sound.getOffset());                  // set the offset
        AL11.alSourcei(source, AL11.AL_LOOPING, loop ? AL11.AL_TRUE : AL11.AL_FALSE);   // set the loop flag
        AL11.alSourcePlay(source);                                                      // play the sound
    }

    @Override
    public void pause(Sound sound) {
        AL11.alSourcePause(getSourcePointer(sound));
    }

    @Override
    public void stop(Sound sound) {
        AL11.alSourceStop(getSourcePointer(sound));
    }

    @Override
    public void sync(Sound sound) {
        var alSound = (ALSound) sound;
        setGain(alSound);
        setPitch(alSound);
        setPosition(alSound);
    }

    private int getSourcePointer(Sound sound) {
        if (sound instanceof ALSound) {
            return ((ALSound) sound).getSourcePointer();
        } else {
            throw new IllegalArgumentException("Unsupported sound type: " + sound.getClass().getSimpleName());
        }
    }

    private void setGain(ALSound sound) {
        AL11.alSourcef(sound.getSourcePointer(), AL11.AL_GAIN, sound.getGain());
    }

    private void setPitch(ALSound sound) {
        AL11.alSourcef(sound.getSourcePointer(), AL11.AL_PITCH, sound.getPitch());
    }

    private void setPosition(ALSound sound) {
        AL11.alSource3f(sound.getSourcePointer(), AL11.AL_POSITION,
                sound.getSpatialPosition().getX(), sound.getSpatialPosition().getY(), 0);
    }
}
