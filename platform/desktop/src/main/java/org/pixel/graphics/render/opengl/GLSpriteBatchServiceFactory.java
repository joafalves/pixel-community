package org.pixel.graphics.render.opengl;

import org.pixel.graphics.render.SpriteBatch;
import org.pixel.graphics.render.SpriteBatchFactory;

public class GLSpriteBatchServiceFactory implements SpriteBatchFactory {

    @Override
    public SpriteBatch create() {
        final var spriteBatch = new GLFastSpriteBatch();
        if (!spriteBatch.init()) {
            throw new RuntimeException("Failed to initialize GLSpriteBatch.");
        }
        return spriteBatch;
    }

    @Override
    public SpriteBatch create(int bufferMaxSize) {
        final var spriteBatch = new GLFastSpriteBatch(bufferMaxSize);
        if (!spriteBatch.init()) {
            throw new RuntimeException("Failed to initialize GLSpriteBatch.");
        }
        return spriteBatch;
    }

    @Override
    public SpriteBatch create(int bufferMaxSize, int shaderTextureCount) {
        final var spriteBatch = new GLFastSpriteBatch(bufferMaxSize, shaderTextureCount);
        if (!spriteBatch.init()) {
            throw new RuntimeException("Failed to initialize GLSpriteBatch.");
        }
        return spriteBatch;
    }
}
