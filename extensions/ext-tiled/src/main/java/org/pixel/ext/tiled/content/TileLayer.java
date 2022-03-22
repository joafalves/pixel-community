package org.pixel.ext.tiled.content;

import org.pixel.ext.tiled.view.GenericTileMapView;

public class TileLayer extends Layer {
    private final int width;
    private final int height;
    private final long[][] tiles;

    public TileLayer(int width, int height, TileMap tileMap) {
        super(tileMap);
        this.width = width;
        this.height = height;

        tiles = new long[height][width];
    }

    public TileLayer(Layer other, int width, int height) {
        super(other);
        this.width = width;
        this.height = height;

        tiles = new long[height][width];

    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public long[][] getTiles() {
        return tiles;
    }

    public void addTile(int x, int y, long gID) {
        tiles[y][x] = gID;
    }

    @Override
    public void draw(GenericTileMapView view) {
        view.draw(this);
    }
}
