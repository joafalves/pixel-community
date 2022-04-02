package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.TiledImageLayer;
import org.pixel.ext.tiled.content.TiledMap;
import org.pixel.ext.tiled.content.TiledObjectGroup;
import org.pixel.ext.tiled.content.TiledTileLayer;

/**
 * A generic map view.
 */
public interface TiledGenericMapView extends TiledView<TiledMap> {
    /**
     * Draws a tile layer.
     *
     * @param layer The layer to draw.
     */
    void draw(TiledTileLayer layer);
    /**
     * Draws an object group layer.
     *
     * @param layer The layer to draw.
     */
    void draw(TiledObjectGroup layer);

    /**
     * Draws an image layer.
     *
     * @param layer The layer to draw.
     */
    void draw(TiledImageLayer layer);
}
