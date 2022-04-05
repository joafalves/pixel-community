package org.pixel.ext.tiled.content;

import org.pixel.ext.tiled.view.TiledGenericMapView;

/**
 * A layer in a TiledMap.
 */
public abstract class TiledLayer {
    protected final TiledMap tileMap;
    private double offsetX;
    private double offsetY;
    private TiledCustomProperties customProperties;

    /**
     * Creates a new TiledLayer.
     *
     * @param tileMap The map this layer belongs to
     */
    public TiledLayer(TiledMap tileMap) {
        this.tileMap = tileMap;
    }

    /**
     * Creates a new TiledLayer copy.
     *
     * @param other The layer to copy.
     */
    public TiledLayer(TiledLayer other) {
        this.tileMap = other.tileMap;
        this.offsetX = other.offsetX;
        this.offsetY = other.offsetY;
        this.customProperties = other.customProperties;
    }

    /**
     * @return The map this layer belongs to.
     */
    public TiledMap getTileMap() {
        return tileMap;
    }

    public TiledCustomProperties getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(TiledCustomProperties customProperties) {
        this.customProperties = customProperties;
    }

    /**
     * @return The horizontal offset of this layer in the map.
     */
    public double getOffsetX() {
        return offsetX;
    }

    /**
     * @param offsetX The horizontal offset of this layer in the map.
     */
    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * @return The vertical offset of this layer in the map.
     */
    public double getOffsetY() {
        return offsetY;
    }

    /**
     * @param offsetY The vertical offset of this layer in the map.
     */
    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * Accepts the GenericMapView Visitor to draw the layer.
     *
     * @param view The view to draw the layer with.
     */
    public abstract void draw(TiledGenericMapView view);
}
