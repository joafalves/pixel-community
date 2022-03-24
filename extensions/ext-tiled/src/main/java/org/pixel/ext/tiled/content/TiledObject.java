package org.pixel.ext.tiled.content;

import org.pixel.ext.tiled.view.TiledGenericObjectGroupView;
import org.pixel.math.Vector2;

public class TiledObject {
    private Vector2 position;
    private float rotation;
    private TiledCustomProperties customProperties;

    public TiledCustomProperties getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(TiledCustomProperties customProperties) {
        this.customProperties = customProperties;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public void draw(TiledObjectGroup group, TiledGenericObjectGroupView view) {

    }
}
