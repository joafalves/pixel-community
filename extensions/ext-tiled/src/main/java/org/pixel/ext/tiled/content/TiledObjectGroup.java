package org.pixel.ext.tiled.content;

import org.pixel.ext.tiled.view.TiledGenericMapView;

import java.util.LinkedHashMap;

public class TiledObjectGroup extends TiledLayer {
    private LinkedHashMap<Integer, TiledObject> objects;

    public TiledObjectGroup(TiledMap tileMap) {
        super(tileMap);
    }

    public TiledObjectGroup(TiledLayer other) {
        super(other);
    }

    @Override
    public void draw(TiledGenericMapView view) {
        view.draw(this);
    }

    public LinkedHashMap<Integer, TiledObject> getObjects() {
        return objects;
    }

    public void setObjects(LinkedHashMap<Integer, TiledObject> objects) {
        this.objects = objects;
    }
}
