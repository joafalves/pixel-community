package org.pixel.ext.tiled.content;

import org.pixel.ext.tiled.view.TiledGenericMapView;

import java.util.LinkedHashMap;

/**
 * A Tiled Object Group Layer.
 */
public class TiledObjectGroup extends TiledLayer {
    private LinkedHashMap<Integer, TiledObject> objects;

    /**
     * Creates a new TiledObjectGroup.
     *
     * @param tileMap The tile map this layer belongs to.
     */
    public TiledObjectGroup(TiledMap tileMap) {
        super(tileMap);
    }

    /**
     * Creates a new TiledObjectGroup, copying all the properties from the given layer.
     *
     * @param other The layer to copy.
     */
    public TiledObjectGroup(TiledLayer other) {
        super(other);
    }

    @Override
    public void draw(TiledGenericMapView view) {
        view.draw(this);
    }

    /**
     * Gets the objects in this layer.
     *
     * @return The objects in this layer.
     */
    public LinkedHashMap<Integer, TiledObject> getObjects() {
        return objects;
    }

    /**
     * Sets the objects in this layer.
     *
     * @param objects The objects in this layer.
     */
    public void setObjects(LinkedHashMap<Integer, TiledObject> objects) {
        this.objects = objects;
    }
}
