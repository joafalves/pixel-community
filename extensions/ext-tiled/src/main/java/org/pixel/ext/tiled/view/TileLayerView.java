package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.TileLayer;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;

public class TileLayerView implements TiledView<TileLayer> {
    private final SpriteBatch spriteBatch;
    private final Boundary boundary;

    public TileLayerView(SpriteBatch spriteBatch, Boundary boundary) {
        this.spriteBatch = spriteBatch;
        this.boundary = boundary;
    }

    @Override
    public void draw(TileLayer layer, long frame) {
        layer.getTileMap().getDrawStrategy().draw(spriteBatch, boundary, layer, frame);
    }
}