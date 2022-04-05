/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.tiled.content;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.ext.tiled.view.TiledDrawStrategy;
import org.pixel.ext.tiled.view.TiledRightDownStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * A Tiled TileMap.
 */
public class TiledMap implements Disposable {
    private int width;
    private int height;
    private int tileWidth;
    private int tileHeight;
    private List<TiledTileSet> tileSets;
    private List<TiledLayer> layers;
    private String renderOrder;
    private TiledDrawStrategy drawStrategy; // This isn't very SOLID-like but it could improve performance
    private TiledCustomProperties customProperties;

    public TiledMap() {
        tileSets = new ArrayList<>();
        layers = new ArrayList<>();
        drawStrategy = new TiledRightDownStrategy();
    }

    @Override
    public void dispose() {
        for (TiledTileSet tileSet : tileSets) {
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

    public List<TiledTileSet> getTileSets() {
        return tileSets;
    }

    public void setTileSets(List<TiledTileSet> tileSets) {
        this.tileSets = tileSets;
    }

    public void addTileSet(TiledTileSet tileSet) {
        tileSets.add(tileSet);
    }

    public List<TiledLayer> getLayers() {
        return layers;
    }

    public void setLayers(List<TiledLayer> layers) {
        this.layers = layers;
    }

    public void addLayer(TiledLayer layer) {
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

    public TiledDrawStrategy getDrawStrategy() {
        return drawStrategy;
    }

    public void setDrawStrategy(TiledDrawStrategy drawStrategy) {
        this.drawStrategy = drawStrategy;
    }

    public TiledCustomProperties getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(TiledCustomProperties customProperties) {
        this.customProperties = customProperties;
    }
}
