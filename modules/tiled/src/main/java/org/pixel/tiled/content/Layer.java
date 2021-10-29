package org.pixel.tiled.content;

import org.pixel.tiled.view.TileMapView;

public abstract class Layer {
    protected final TileMap tileMap;
    private double offsetX;
    private double offsetY;
    private TiledCustomProperties customProperties;

    public Layer(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    public Layer(Layer other) {
        this.tileMap = other.tileMap;
        this.offsetX = other.offsetX;
        this.offsetY = other.offsetY;
        this.customProperties = other.customProperties;
    }

    public TileMap getTileMap() {
        return tileMap;
    }

    public TiledCustomProperties getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(TiledCustomProperties customProperties) {
        this.customProperties = customProperties;
    }

    public double getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    public abstract void draw(TileMapView view);
}
