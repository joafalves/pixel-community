/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.math.Rectangle;

import java.nio.ByteBuffer;

public abstract class RenderEngine implements Disposable {

    protected Rectangle viewportDimensions;
    protected float pixelRatio;

    /**
     * Constructor.
     *
     * @param viewportDimensions Viewport dimensions.
     */
    public RenderEngine(Rectangle viewportDimensions) {
        this.viewportDimensions = viewportDimensions;
        this.pixelRatio = 1.f;
    }

    /**
     * Set the viewport dimensions.
     *
     * @param viewport The viewport dimensions.
     */
    public void setViewport(Rectangle viewport) {
        setViewport(viewport.getX(), viewport.getY(), viewport.getWidth(), viewport.getHeight());
    }

    /**
     * Set the viewport dimensions.
     *
     * @param x      Top left x coordinate.
     * @param y      Top left y coordinate.
     * @param width  The width.
     * @param height The height.
     */
    public void setViewport(float x, float y, float width, float height) {
        this.viewportDimensions.set(x, y, width, height);
    }

    /**
     * Begin render frame.
     */
    public abstract void begin();

    /**
     * End render frame.
     */
    public abstract void end();

    /**
     * Add font data to the engine.
     *
     * @param fontData The font data.
     * @param fontName The font name.
     */
    public abstract void addFont(ByteBuffer fontData, String fontName);

    /**
     * Get the current pixel ratio.
     *
     * @return The pixel ratio.
     */
    public float getPixelRatio() {
        return pixelRatio;
    }

    /**
     * Set the current pixel ratio.
     *
     * @param pixelRatio The pixel ratio.
     */
    public void setPixelRatio(float pixelRatio) {
        this.pixelRatio = pixelRatio;
    }
}
