package org.pixel.tiled.view;

import org.pixel.graphics.render.SpriteBatch;
import org.pixel.tiled.content.Layer;
import org.pixel.tiled.content.TileMap;

public class TileMapView implements TiledView<TileMap> {
    TiledView<Layer> layerView;

    public TileMapView() {
        layerView = new LayerView();
    }

    public TileMapView(TiledView<Layer> layerView) {
        this.layerView = layerView;
    }

    @Override
    public void draw(SpriteBatch spriteBatch, TileMap tileMap) {
        for (Layer layer : tileMap.getLayers()) {
            layerView.draw(spriteBatch, layer);
        }
    }
}
