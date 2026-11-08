package org.pixel.graphics.pass;

import org.pixel.graphics.backend.RenderBackend;
import org.pixel.graphics.backend.RenderTarget;

/**
 * What a {@link PostEffect} gets to work with while the pipeline runs it.
 */
public interface PostContext {

    /**
     * @return The render backend.
     */
    RenderBackend backend();

    /**
     * A scratch target for multi-pass effects (owned by the pipeline, reused every frame; contents are undefined
     * until written). Each effect of a chain may use slots from 0 on; slots are shared between effects.
     *
     * @param slot   Which scratch target (0, 1, 2, ...).
     * @param width  Width in pixels.
     * @param height Height in pixels.
     */
    RenderTarget temporary(int slot, int width, int height);

    /**
     * Run another effect (to build multi-pass effects from simpler ones, like {@link Effects.Bloom}).
     */
    void apply(PostEffect effect, org.pixel.content.Texture source, RenderTarget destination, boolean blend);
}
