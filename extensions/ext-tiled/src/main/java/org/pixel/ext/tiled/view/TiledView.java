package org.pixel.ext.tiled.view;

public interface TiledView<T> {
    void draw(T element, long currentMs);
}
