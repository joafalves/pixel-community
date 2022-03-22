package org.pixel.ext.tiled.content;

import org.pixel.ext.tiled.view.GenericTileMapView;

import java.util.LinkedHashMap;

public class TiledObjectGroup extends Layer {
    private LinkedHashMap<Integer, TiledObject> objects;

    public TiledObjectGroup(TileMap tileMap) {
        super(tileMap);
    }

    public TiledObjectGroup(Layer other) {
        super(other);
    }

    @Override
    public void draw(GenericTileMapView view) {
        view.draw(this);
    }

    public LinkedHashMap<Integer, TiledObject> getObjects() {
        return objects;
    }

    public void setObjects(LinkedHashMap<Integer, TiledObject> objects) {
        this.objects = objects;
    }
}
