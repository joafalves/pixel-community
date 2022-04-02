package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.TiledTileLayer;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;

/**
 * A view for a tiled tile layer.
 */
public class TiledLayerView implements TiledView<TiledTileLayer> {
    private final SpriteBatch spriteBatch;
    private final Boundary boundary;

    /**
     * Creates a new tiled layer view.
     *
     * @param spriteBatch The sprite batch to use.
     * @param boundary    The boundary to draw within.
     */
    public TiledLayerView(SpriteBatch spriteBatch, Boundary boundary) {
        this.spriteBatch = spriteBatch;
        this.boundary = boundary;
    }

    /**
     * Draws the layer.
     *
     * @param layer     The layer to draw.
     * @param currentMs The current time in milliseconds.
     */
    @Override
    public void draw(TiledTileLayer layer, long currentMs) {
        layer.getTileMap().getDrawStrategy().draw(spriteBatch, boundary, layer, currentMs);
    }
}
