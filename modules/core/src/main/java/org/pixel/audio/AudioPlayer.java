package org.pixel.audio;

import org.pixel.commons.factory.FactoryProvider;
import org.pixel.content.Sound;

public interface AudioPlayer {

    /**
     * Create a platform-specific AudioPlayer instance.
     *
     * <p>This factory method delegates to the registered {@link AudioPlayerFactory} to create
     * the appropriate platform-specific implementation (e.g., ALAudioPlayer on desktop).
     *
     * <p>Example usage:
     * <pre>
     * AudioPlayer audio = AudioPlayer.create();
     * audio.play(sound);
     * </pre>
     *
     * @return A new AudioPlayer instance appropriate for the current platform
     */
    static AudioPlayer create() {
        return FactoryProvider.get(AudioPlayerFactory.class).create();
    }

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
