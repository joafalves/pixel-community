package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.TiledTileLayer;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.content.TiledImageLayer;
import org.pixel.ext.tiled.content.TiledObjectGroup;

public interface TiledGenericMapView extends TiledView<TiledMap> {
    default void draw(TiledTileLayer layer) {

    }

    default void draw(TiledObjectGroup layer) {

    }

    default void draw(TiledImageLayer layer) {

    }
}
