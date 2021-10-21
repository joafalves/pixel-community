package org.pixel.tiled.view;

import org.pixel.graphics.render.SpriteBatch;
import org.pixel.tiled.content.Layer;
import org.pixel.tiled.content.TileMap;
import org.pixel.tiled.content.TiledObjectGroup;

public class TileMapView implements TiledView<TileMap> {
    TiledView<Layer> layerView;
    TiledView<TiledObjectGroup> groupView;

    public TileMapView() {
        layerView = new LayerView();
        groupView = new TiledObjectGroupView();
    }

    public TileMapView(TiledView<Layer> layerView, TiledView<TiledObjectGroup> groupView) {
        this.layerView = layerView;
        this.groupView = groupView;
    }

    @Override
    public void draw(SpriteBatch spriteBatch, TileMap tileMap) {
        for (Layer layer : tileMap.getLayers()) {
            layerView.draw(spriteBatch, layer);
        }

        for (TiledObjectGroup group : tileMap.getObjectGroups()) {
            groupView.draw(spriteBatch, group);
        }
    }
}
