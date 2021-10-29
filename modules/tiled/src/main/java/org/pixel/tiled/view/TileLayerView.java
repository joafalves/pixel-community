package org.pixel.tiled.view;

import org.pixel.graphics.render.SpriteBatch;
import org.pixel.tiled.content.TileLayer;

public class TileLayerView implements TiledView<TileLayer> {
    private final SpriteBatch spriteBatch;

    public TileLayerView(SpriteBatch spriteBatch) {
        this.spriteBatch = spriteBatch;
    }

    @Override
    public void draw(TileLayer layer, long frame) {
        layer.getTileMap().getDrawStrategy().draw(spriteBatch, layer, frame);
    }
}
