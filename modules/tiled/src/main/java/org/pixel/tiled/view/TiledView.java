package org.pixel.tiled.view;

import org.pixel.graphics.render.SpriteBatch;

public interface TiledView<T> { // todo: change name to TiledView
    void draw(SpriteBatch spriteBatch, T element);
}
