/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.graphics.render;

import pixel.commons.lifecycle.Disposable;
import pixel.math.Rectangle;

import java.nio.ByteBuffer;

public abstract class RenderEngine implements Disposable {

    protected Rectangle viewportDimensions;
    protected float pixelRatio;

    /**
     * Constructor
     */
    public RenderEngine(Rectangle viewportDimensions) {
        this.viewportDimensions = viewportDimensions;
        this.pixelRatio = 1.f;
    }

    /**
     * @param viewport
     */
    public void setViewport(Rectangle viewport) {
        setViewport(viewport.getX(), viewport.getY(), viewport.getWidth(), viewport.getHeight());
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void setViewport(float x, float y, float width, float height) {
        this.viewportDimensions.set(x, y, width, height);
    }

    /**
     * Begin render frame
     */
    public abstract void begin();

    /**
     * End render frame
     */
    public abstract void end();

    /**
     * @param fontData
     * @param fontName
     */
    public abstract void addFont(ByteBuffer fontData, String fontName);

    public float getPixelRatio() {
        return pixelRatio;
    }

    public void setPixelRatio(float pixelRatio) {
        this.pixelRatio = pixelRatio;
    }
}
