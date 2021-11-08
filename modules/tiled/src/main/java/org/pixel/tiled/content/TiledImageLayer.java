package org.pixel.tiled.content;

import org.pixel.content.Texture;
import org.pixel.tiled.view.GenericTileMapView;

public class TiledImageLayer extends Layer{
    Texture image;

    public TiledImageLayer(TileMap tileMap) {
        super(tileMap);
    }

    public TiledImageLayer(Layer other) {
        super(other);
    }

    public void setImage(Texture image) {
        this.image = image;
    }

    public Texture getImage() {
        return image;
    }

    @Override
    public void draw(GenericTileMapView view) {
        view.draw(this);
    }
}
