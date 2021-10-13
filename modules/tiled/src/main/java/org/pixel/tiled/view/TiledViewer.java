package org.pixel.tiled.view;

import org.pixel.graphics.render.SpriteBatch;

public interface TiledViewer<T> {
    void draw(SpriteBatch spriteBatch, T element);
}
