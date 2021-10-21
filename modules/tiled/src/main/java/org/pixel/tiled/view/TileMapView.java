package org.pixel.tiled.view;

import org.pixel.graphics.render.SpriteBatch;
import org.pixel.tiled.content.Layer;
import org.pixel.tiled.content.TileLayer;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TiledObjectGroup;

public class TileMapView implements TiledView<TileMap> {
    TiledView<TileLayer> layerView;
    TiledView<TiledObjectGroup> groupView;

    public TileMapView() {
        layerView = new TileLayerView();
        groupView = new TiledObjectGroupView();
    }

    public TileMapView(TiledView<TileLayer> layerView, TiledView<TiledObjectGroup> groupView) {
        this.layerView = layerView;
        this.groupView = groupView;
    }

    public void draw(SpriteBatch spriteBatch, TileLayer layer) {
        layerView.draw(spriteBatch, layer);
    }

    public void draw(SpriteBatch spriteBatch, TiledObjectGroup layer) {
        groupView.draw(spriteBatch, layer);
    }

    @Override
    public void draw(SpriteBatch spriteBatch, TileMap tileMap) {
        for (Layer layer : tileMap.getLayers()) {
            layer.draw(spriteBatch, this);
        }
    }
}
