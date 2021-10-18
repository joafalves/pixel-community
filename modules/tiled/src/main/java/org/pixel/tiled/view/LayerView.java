package org.pixel.tiled.view;

import org.pixel.graphics.render.SpriteBatch;
import org.pixel.tiled.content.Layer;

public class LayerView implements TiledView<Layer> {
    @Override
    public void draw(SpriteBatch spriteBatch, Layer layer) {
        layer.getTileMap().getDrawStrategy().draw(spriteBatch, layer);
    }
}
