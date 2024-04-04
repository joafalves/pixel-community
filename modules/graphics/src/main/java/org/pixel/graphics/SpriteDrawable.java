package org.pixel.graphics;

import org.pixel.commons.DeltaTime;
import org.pixel.graphics.render.SpriteBatch;

public interface SpriteDrawable {

    /**
     * Draw function.
     *
     * @param delta Time since last draw.
     * @param spriteBatch SpriteBatch to draw to.
     */
    void draw(DeltaTime delta, SpriteBatch spriteBatch);
}
