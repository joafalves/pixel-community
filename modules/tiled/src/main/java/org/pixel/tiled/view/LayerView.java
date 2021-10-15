package org.pixel.tiled.view;

import org.pixel.graphics.render.SpriteBatch;
import org.pixel.tiled.content.Layer;

public class LayerView implements TiledViewer<Layer> {
    @Override
    public void draw(SpriteBatch spriteBatch, Layer layer) {
        DrawStrategyFactory factory = new DrawStrategyFactory();

        factory.createDrawStrategy(layer.getTileMap()).draw(spriteBatch, layer);
    }
}
