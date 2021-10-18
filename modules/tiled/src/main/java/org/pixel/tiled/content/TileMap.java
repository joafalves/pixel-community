/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.tiled.content;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.tiled.view.DrawStrategy;

import java.util.ArrayList;
import java.util.List;

public class TileMap implements Disposable {
    private int width;
    private int height;
    private int tileWidth;
    private int tileHeight;
    private List<TileSet> tileSets;
    private List<Layer> layers;
    private String renderOrder;
    private DrawStrategy drawStrategy; // This isn't very SOLID-like but it could improve performance
    private TiledCustomProperties customProperties;
    private List<TiledObjectGroup> objectGroups;

    public TileMap() {
        tileSets = new ArrayList<>();
        layers = new ArrayList<>();
    }

    public List<TiledObjectGroup> getObjectGroups() {
        return objectGroups;
    }

    public void setObjectGroups(List<TiledObjectGroup> objectGroups) {
        this.objectGroups = objectGroups;
    }

    @Override
    public void dispose() {
        for (TileSet tileSet : tileSets) {
            tileSet.dispose();
        }
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<TileSet> getTileSets() {
        return tileSets;
    }

    public void setTileSets(List<TileSet> tileSets) {
        this.tileSets = tileSets;
    }

    public void addTileSet(TileSet tileSet) {
        tileSets.add(tileSet);
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }

    public void addLayer(Layer layer) {
        this.layers.add(layer);
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public String getRenderOrder() {
        return renderOrder;
    }

    public void setRenderOrder(String renderOrder) {
        this.renderOrder = renderOrder;
    }

    public DrawStrategy getDrawStrategy() {
        return drawStrategy;
    }

    public void setDrawStrategy(DrawStrategy drawStrategy) {
        this.drawStrategy = drawStrategy;
    }

    public TiledCustomProperties getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(TiledCustomProperties customProperties) {
        this.customProperties = customProperties;
    }
}
