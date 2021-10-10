package org.pixel.tiled.content;

import org.pixel.content.Texture;
import org.pixel.math.Vector2;

public class TileSet {
    private int tileWidth, tileHeight, tileCount, columns;
    private Texture texture;

    public TileSet(int tileWidth, int tileHeight, int tileCount, int columns) {
        this.tileCount = tileCount;
        this.tileHeight = tileHeight;
        this.tileWidth = tileWidth;
        this.columns = columns;
    }

    public int getColumns() {
        return columns;
    }

    public int getTileCount() {
        return tileCount;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public Texture getTexture() {
        return texture;
    }
}
