package org.pixel.tiled.view;

public interface TiledView<T> {
    void draw(T element, long frame);
}
