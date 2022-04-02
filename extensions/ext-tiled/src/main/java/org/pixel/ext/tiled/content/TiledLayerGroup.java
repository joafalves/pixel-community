package org.pixel.ext.tiled.content;

import org.pixel.ext.tiled.view.TiledGenericMapView;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of TiledLayers.
 */
public class TiledLayerGroup extends TiledLayer {
    private List<TiledLayer> layers;

    /**
     * Creates a new TiledLayerGroup.
     * @param tileMap The tile map this layer belongs to.
     */
    public TiledLayerGroup(TiledMap tileMap) {
        super(tileMap);

        layers = new ArrayList<>();
    }

    /**
     * Creates a new TiledLayerGroup, copying all properties from the given layer.
     * @param other The layer to copy.
     */
    public TiledLayerGroup(TiledLayer other) {
        super(other);
    }

    /**
     * Returns the layers in this group.
     * @return The layers in this group.
     */
    public List<TiledLayer> getLayers() {
        return layers;
    }

    /**
     * Sets the layers in this group.
     * @param layers The layers to set.
     */
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
