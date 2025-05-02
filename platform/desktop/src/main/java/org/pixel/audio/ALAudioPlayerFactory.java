package org.pixel.audio;

import org.pixel.commons.service.ServiceFactory;

public class ALAudioPlayerFactory implements ServiceFactory<AudioPlayer> {
    @Override
    public AudioPlayer get() {
        return new ALAudioPlayer();
    }
}
