package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.TileLayer;
import org.pixel.ext.tiled.content.TileMap;
import org.pixel.ext.tiled.content.TiledImageLayer;
import org.pixel.ext.tiled.content.TiledObjectGroup;

public interface GenericTileMapView extends TiledView<TileMap> {
    default void draw(TileLayer layer) {

    }

    default void draw(TiledObjectGroup layer) {

    }

    default void draw(TiledImageLayer layer) {

    }
}
