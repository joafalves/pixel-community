package org.pixel.tiled.content;

import org.pixel.graphics.render.SpriteBatch;
import org.pixel.tiled.view.TileMapView;

import java.util.LinkedHashMap;

public class TiledObjectGroup extends Layer {
    private LinkedHashMap<Integer, DrawableTiledObject> objects;

    public TiledObjectGroup(TileMap tileMap) {
        super(tileMap);
    }

    public TiledObjectGroup(Layer other) {
        super(other);
    }

    @Override
    public void draw(SpriteBatch spriteBatch, TileMapView view) {
        view.draw(spriteBatch, this);
    }

    public LinkedHashMap<Integer, DrawableTiledObject> getObjects() {
        return objects;
    }

    public void setObjects(LinkedHashMap<Integer, DrawableTiledObject> objects) {
        this.objects = objects;
    }
}
