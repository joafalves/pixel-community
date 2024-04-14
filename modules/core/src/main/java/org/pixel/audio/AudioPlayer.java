package org.pixel.audio;

import org.pixel.content.Sound;

public interface AudioPlayer {

    /**
     * Play the sound.
     *
     * @param sound The sound to play
     */
    void play(Sound sound);

    /**
     * Play the sound.
     *
     * @param sound The sound to play
     * @param loop  Whether the sound should loop
     */
    void play(Sound sound, boolean loop);

    /**
     * Pause the sound.
     *
     * @param sound The sound to pause
     */
    void pause(Sound sound);

    /**
     * Stop the sound.
     *
     * @param sound The sound to stop
     */
    void stop(Sound sound);

    /**
     * Sync the sound properties with the audio player.
     *
     * @param sound The sound to sync.
     */
    void sync(Sound sound);

}
