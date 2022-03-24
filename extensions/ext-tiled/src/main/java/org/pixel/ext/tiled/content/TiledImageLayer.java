package org.pixel.ext.tiled.content;

import org.pixel.content.Texture;
import org.pixel.ext.tiled.view.GenericTileMapView;

public class TiledImageLayer extends TiledLayer {
    Texture image;

    public TiledImageLayer(TiledMap tileMap) {
        super(tileMap);
    }

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
    public void draw(GenericTileMapView view) {
        view.draw(this);
    }
}
