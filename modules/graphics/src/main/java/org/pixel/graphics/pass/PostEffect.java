package org.pixel.graphics.pass;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.content.Texture;
import org.pixel.graphics.backend.RenderTarget;

/**
 * A full-screen image effect used by {@link RenderPipeline.Builder#post} passes (see {@link Effects} for the built-in
 * ones and {@link ShaderEffect} to write your own). Effects keep their parameters, which can change every frame.
 * Disposing an effect releases its GPU programs.
 */
public abstract class PostEffect implements Disposable {

    /**
     * Render the effect.
     *
     * @param context     Backend access and scratch targets.
     * @param source      The image to process (premultiplied colors).
     * @param destination Where to write: a target, or {@code null} for the screen.
     * @param blend       Composite over the destination ({@code true}, premultiplied) or replace it.
     */
    protected abstract void apply(PostContext context, Texture source, RenderTarget destination, boolean blend);
}
