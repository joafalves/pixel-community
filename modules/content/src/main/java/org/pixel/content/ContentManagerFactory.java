package org.pixel.content;

import org.pixel.commons.factory.Factory;

/**
 * Factory interface for creating platform-specific ContentManager instances.
 *
 * <p>ContentManager instances are created with platform-specific importers
 * automatically configured based on the current platform (e.g., GLTextureImporter on desktop).
 */
public interface ContentManagerFactory extends Factory {

    /**
     * Create a ContentManager with platform-specific importers.
     *
     * @return A new ContentManager instance with appropriate importers for the current platform
     */
    ContentManager create();
}
