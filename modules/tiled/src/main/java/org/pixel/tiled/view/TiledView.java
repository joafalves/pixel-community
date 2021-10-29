package org.pixel.tiled.view;

import org.pixel.graphics.render.SpriteBatch;

public interface TiledView<T> {
    void draw(SpriteBatch spriteBatch, T element, long frame);
}
