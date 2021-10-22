package org.pixel.tiled.view;

import org.pixel.graphics.render.SpriteBatch;
import org.pixel.tiled.content.TileLayer;

public class TileLayerView implements TiledView<TileLayer> {
    @Override
    public void draw(SpriteBatch spriteBatch, TileLayer layer) {
        layer.getTileMap().getDrawStrategy().draw(spriteBatch, layer);
    }
}
