package org.pixel.ext.tiled.content;

import org.pixel.ext.tiled.view.TiledGenericMapView;

public class TiledTileLayer extends TiledLayer {
    private final int width;
    private final int height;
    private final long[][] tiles;

    public TiledTileLayer(int width, int height, TiledMap tileMap) {
        super(tileMap);
        this.width = width;
        this.height = height;

        tiles = new long[height][width];
    }

    public TiledTileLayer(TiledLayer other, int width, int height) {
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
    public void draw(TiledGenericMapView view) {
        view.draw(this);
    }
}
