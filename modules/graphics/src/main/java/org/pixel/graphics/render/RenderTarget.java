package org.pixel.graphics.render;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.content.Texture;

/**
 * Defines the contract for a render target, which is essentially an off-screen texture that can be rendered to.
 */
public interface RenderTarget extends Disposable {
    /**
     * Gets the width of the render target.
     *
     * @return The width in pixels.
     */
    int getWidth();

    /**
     * Gets the height of the render target.
     *
     * @return The height in pixels.
     */
    int getHeight();

    /**
     * Gets the texture that this target renders to.
     *
     * @return The texture object.
     */
    Texture getTexture();

    /**
     * Prepares the render target for drawing. This typically binds the underlying framebuffer.
     */
    void begin();

    /**
     * Finalizes drawing to the render target. This typically unbinds the underlying framebuffer.
     */
    void end();
}
