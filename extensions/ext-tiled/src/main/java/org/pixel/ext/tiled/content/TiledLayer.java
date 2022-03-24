package org.pixel.ext.tiled.content;

import org.pixel.ext.tiled.view.GenericTileMapView;

public abstract class TiledLayer {
    protected final TiledMap tileMap;
    private double offsetX;
    private double offsetY;
    private TiledCustomProperties customProperties;

    public TiledLayer(TiledMap tileMap) {
        this.tileMap = tileMap;
    }

    public TiledLayer(TiledLayer other) {
        this.tileMap = other.tileMap;
        this.offsetX = other.offsetX;
        this.offsetY = other.offsetY;
        this.customProperties = other.customProperties;
    }

    public TiledMap getTileMap() {
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

    public abstract void draw(GenericTileMapView view);
}
