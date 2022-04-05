package org.pixel.ext.tiled.content;

import org.pixel.ext.tiled.view.TiledGenericMapView;

/**
 * A TiledTileLayer is a TiledLayer that contains tiles.
 */
public class TiledTileLayer extends TiledLayer {
    private final int width;
    private final int height;
    private final long[][] tiles;

    /**
     * Creates a new TiledTileLayer
     *
     * @param width   The width in tiles.
     * @param height  The height in tiles.
     * @param tileMap The tilemap this layer belongs to.
     */
    public TiledTileLayer(int width, int height, TiledMap tileMap) {
        super(tileMap);
        this.width = width;
        this.height = height;

        tiles = new long[height][width];
    }

    /**
     * Creates a new TiledTileLayer copying attributes from another layer.
     *
     * @param other  The other layer.
     * @param width  The width in tiles.
     * @param height The height in tiles.
     */
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
