package org.pixel.tiled.view;

import org.pixel.tiled.content.TileLayer;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TiledObjectGroup;

public interface GenericTileMapView extends TiledView<TileMap> {
    default void draw(TileLayer layer) {

    }

    default void draw(TiledObjectGroup layer) {

    }
}
