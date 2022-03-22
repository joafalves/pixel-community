package org.pixel.ext.tiled.content;

import org.pixel.ext.tiled.view.GenericTileMapView;

import java.util.ArrayList;
import java.util.List;

public class LayerGroup extends Layer {
    List<Layer> layers;

    public LayerGroup(TileMap tileMap) {
        super(tileMap);

        layers = new ArrayList<>();
    }

    public LayerGroup(Layer other) {
        super(other);
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(List<Layer> layers) {
        this.layers = layers;

        for (Layer layer : layers) {
            layer.setOffsetX(layer.getOffsetX() + this.getOffsetX());
            layer.setOffsetY(layer.getOffsetY() + this.getOffsetY());
        }
    }

    @Override
    public void draw(GenericTileMapView view) {
        for (Layer layer : layers) {
            layer.draw(view);
        }
    }
}