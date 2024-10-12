package org.pixel.audio;

import org.pixel.commons.ServiceFactory;

public class ALAudioPlayerFactory implements ServiceFactory<AudioPlayer> {
    @Override
    public AudioPlayer create() {
        return new ALAudioPlayer();
    }
}
