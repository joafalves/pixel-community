package org.pixel.audio;

import org.pixel.commons.factory.Factory;

/**
 * Factory interface for creating platform-specific AudioPlayer instances.
 *
 * <p>AudioPlayer provides audio playback functionality for the game engine,
 * with platform-specific implementations (e.g., OpenAL on desktop).
 */
public interface AudioPlayerFactory extends Factory {

    /**
     * Create an AudioPlayer instance.
     *
     * @return A new AudioPlayer instance appropriate for the current platform
     */
    AudioPlayer create();
}
