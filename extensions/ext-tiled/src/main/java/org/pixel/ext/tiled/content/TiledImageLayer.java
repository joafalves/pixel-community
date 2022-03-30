package org.pixel.ext.tiled.content;

import org.pixel.content.Texture;
import org.pixel.ext.tiled.view.TiledGenericMapView;

/**
 * A TileMap layer that displays an image.
 */
public class TiledImageLayer extends TiledLayer {
    Texture image;

    /**
     * Creates a new TiledImageLayer.
     * @param tileMap The tile map this layer belongs to.
     */
    public TiledImageLayer(TiledMap tileMap) {
        super(tileMap);
    }

    /**
     * Creates a new TiledImageLayer, copying all the properties from the given layer.
     * @param other The layer to copy.
     */
    public TiledImageLayer(TiledLayer other) {
        super(other);
    }

    public Texture getImage() {
        return image;
    }

    public void setImage(Texture image) {
        this.image = image;
    }

    @Override
    public void draw(TiledGenericMapView view) {
        view.draw(this);
    }
}
