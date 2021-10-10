package org.pixel.tiled.content;

import org.pixel.content.Texture;

public class TileSet {
    private final int tileWidth;
    private final int tileHeight;
    private final int tileCount;
    private final int columns;
    private final Texture texture;

    public TileSet(int tileWidth, int tileHeight, int tileCount, int columns, Texture texture) {
        this.tileCount = tileCount;
        this.tileHeight = tileHeight;
        this.tileWidth = tileWidth;
        this.columns = columns;
        this.texture = texture;
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
