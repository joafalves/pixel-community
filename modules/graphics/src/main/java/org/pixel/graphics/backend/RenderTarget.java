package org.pixel.graphics.backend;

import org.pixel.commons.Color;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.content.Texture;

/**
 * An offscreen image passes can draw into (created by {@link RenderBackend#createTarget}). Its {@link #getTexture()
 * texture} can be drawn like any image (minimaps, portals, mirrors) or processed by post effects.
 * <p>
 * The texture uses the same coordinates as image textures (v = 0 is the top) and holds <b>premultiplied</b> colors,
 * which keeps semi-transparent edges correct when the target is drawn over something else; the renderer handles
 * that automatically.
 */
public interface RenderTarget extends Disposable {

    /** Width in pixels. */
    int getWidth();

    /** Height in pixels. */
    int getHeight();

    /** The color texture (owned by the target). */
    Texture getTexture();

    /**
     * Reallocate the storage at a new size (contents are lost). Does nothing if the size is unchanged.
     */
    void resize(int width, int height);

    /**
     * The color the target is cleared to when a pipeline first draws into it in a frame (default transparent).
     */
    Color getClearColor();

    void setClearColor(Color color);
}
