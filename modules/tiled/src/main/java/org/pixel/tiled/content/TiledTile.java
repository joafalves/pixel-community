package org.pixel.tiled.content;

import java.util.HashMap;

public class TiledTile {
    private TiledCustomProperties properties;
    private HashMap<Integer, TiledObject> colliders;

    public TiledCustomProperties getProperties() {
        return properties;
    }

    public void setProperties(TiledCustomProperties properties) {
        this.properties = properties;
    }

    public HashMap<Integer, TiledObject> getColliders() {
        return colliders;
    }

    public void setColliders(HashMap<Integer, TiledObject> colliders) {
        this.colliders = colliders;
    }
}
