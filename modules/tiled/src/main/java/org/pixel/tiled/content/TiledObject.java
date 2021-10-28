package org.pixel.tiled.content;

import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;
import org.pixel.tiled.view.TiledObjectGroupView;

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

    public void draw(SpriteBatch spriteBatch, TiledObjectGroup group, TiledObjectGroupView view) {

    }
}
