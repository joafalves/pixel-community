package org.pixel.graphics.render;

import org.pixel.commons.factory.Factory;

/**
 * Factory interface for creating platform-specific SpriteBatch instances.
 *
 * <p>This factory provides multiple {@code create()} methods with different parameters
 * to allow flexible configuration of SpriteBatch instances.
 */
public interface SpriteBatchFactory extends Factory {

    /**
     * Create a SpriteBatch with default settings.
     *
     * @return A new SpriteBatch instance
     */
    SpriteBatch create();

    /**
     * Create a SpriteBatch with a custom buffer size.
     *
     * @param bufferMaxSize The maximum number of sprites that can be batched
     * @return A new SpriteBatch instance
     */
    SpriteBatch create(int bufferMaxSize);

    /**
     * Create a SpriteBatch with custom buffer size and texture unit count.
     *
     * @param bufferMaxSize      The maximum number of sprites that can be batched
     * @param shaderTextureCount The number of texture units to use (0 for auto-detect)
     * @return A new SpriteBatch instance
     */
    SpriteBatch create(int bufferMaxSize, int shaderTextureCount);
}
