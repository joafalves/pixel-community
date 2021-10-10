/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.tiled.content;

import org.pixel.commons.lifecycle.Disposable;

import java.util.ArrayList;
import java.util.List;

public class TileMap implements Disposable {
    private int width;
    private int height;
    private List<TileSet> tileSets;
    private List<Layer> layers;

    public TileMap() {
        tileSets = new ArrayList<>();
        layers = new ArrayList<>();
    }

    @Override
    public void dispose() {

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
}
