package org.pixel.tiled.content;

import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Vector2;
import org.pixel.tiled.view.TiledObjectGroupView;

public class TiledObject {
    private final TileMap tileMap;
    private Vector2 position;
    private float width;
    private float height;
    private float rotation;
    private TiledCustomProperties customProperties;

    public TiledObject(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    public TiledCustomProperties getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(TiledCustomProperties customProperties) {
        this.customProperties = customProperties;
    }

    public TileMap getTileMap() {
        return tileMap;
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

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void draw(SpriteBatch spriteBatch, TiledObjectGroup group, TiledObjectGroupView view) {
    }
}
