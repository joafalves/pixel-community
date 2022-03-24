package org.pixel.ext.tiled.content;

import org.pixel.ext.tiled.view.TiledGenericMapView;

import java.util.ArrayList;
import java.util.List;

public class TiledLayerGroup extends TiledLayer {
    List<TiledLayer> layers;

    public TiledLayerGroup(TiledMap tileMap) {
        super(tileMap);

        layers = new ArrayList<>();
    }

    public TiledLayerGroup(TiledLayer other) {
        super(other);
    }

    public List<TiledLayer> getLayers() {
        return layers;
    }

    public void setLayers(List<TiledLayer> layers) {
        this.layers = layers;

        for (TiledLayer layer : layers) {
            layer.setOffsetX(layer.getOffsetX() + this.getOffsetX());
            layer.setOffsetY(layer.getOffsetY() + this.getOffsetY());
        }
    }

    @Override
    public void draw(TiledGenericMapView view) {
        for (TiledLayer layer : layers) {
            layer.draw(view);
        }
    }
}
