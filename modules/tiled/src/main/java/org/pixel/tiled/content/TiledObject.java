package org.pixel.tiled.content;

import org.pixel.math.Vector2;

public class TiledObject {
    private Vector2 position;
    private float width;
    private float height;
    private float rotation;
    private long gID;
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

    public float getgID() {
        return gID;
    }

    public void setgID(long gID) {
        this.gID = gID;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setWidth(float width) {
        this.width = width;
    }
}
