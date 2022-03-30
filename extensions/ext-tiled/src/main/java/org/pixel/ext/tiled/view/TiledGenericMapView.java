package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.TiledTileLayer;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.content.TiledImageLayer;
import org.pixel.ext.tiled.content.TiledObjectGroup;

/**
 * A generic map view.
 */
public interface TiledGenericMapView extends TiledView<TiledMap> {
    /**
     * Draws a tile layer.
     * @param layer The layer to draw.
     */
    default void draw(TiledTileLayer layer) {

    }

    /**
     * Draws an object group layer.
     * @param layer The layer to draw.
     */
    default void draw(TiledObjectGroup layer) {

    }

    /**
     * Draws an image layer.
     * @param layer The layer to draw.
     */
    default void draw(TiledImageLayer layer) {

    }
}
