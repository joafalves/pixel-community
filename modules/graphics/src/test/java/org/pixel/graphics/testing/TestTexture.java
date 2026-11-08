package org.pixel.graphics.testing;

import org.pixel.content.Texture;

/** GPU-free texture for tests. */
public class TestTexture extends Texture {

    public TestTexture(int width, int height) {
        super(width, height);
    }

    @Override
    public void dispose() {
    }
}
