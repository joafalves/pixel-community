package org.pixel.audio;

public class ALAudioPlayerFactory implements AudioPlayerFactory {
    @Override
    public AudioPlayer create() {
        return new ALAudioPlayer();
    }
}
