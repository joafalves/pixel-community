package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.TiledTileLayer;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;

public class TiledLayerView implements TiledView<TiledTileLayer> {
    private final SpriteBatch spriteBatch;
    private final Boundary boundary;

    public TiledLayerView(SpriteBatch spriteBatch, Boundary boundary) {
        this.spriteBatch = spriteBatch;
        this.boundary = boundary;
    }

    @Override
    public void draw(TiledTileLayer layer, long frame) {
        layer.getTileMap().getDrawStrategy().draw(spriteBatch, boundary, layer, frame);
    }
}
