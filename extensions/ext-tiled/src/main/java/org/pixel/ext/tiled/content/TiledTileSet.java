package org.pixel.ext.tiled.content;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.content.Texture;
import org.pixel.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * A Tiled tile set.
 */
public class TiledTileSet implements Disposable {
    private final int tileWidth;
    private final int tileHeight;
    private final int tileCount;
    private final int columns;
    private final Texture texture;
    private final List<TiledTile> tiles;
    private int firstGId;
    private TiledCustomProperties customProperties;

    /**
     * Creates a new TiledTileSet.
     *
     * @param tileWidth  The width of a tile in pixels.
     * @param tileHeight The height of a tile in pixels.
     * @param tileCount  The number of tiles in the set.
     * @param columns    The number of columns in the set.
     * @param texture    The tile set texture atlas.
     */
    public TiledTileSet(int tileWidth, int tileHeight, int tileCount, int columns, Texture texture) {
        this.tileCount = tileCount;
        this.tileHeight = tileHeight;
        this.tileWidth = tileWidth;
        this.columns = columns;
        this.texture = texture;
        this.tiles = new ArrayList<>(tileCount);

        for (int i = 0; i < tileCount; i++) {
            tiles.add(null);
        }
    }

    /**
     * @return The list of TiledTiles in the tile set.
     */
    public List<TiledTile> getTiles() {
        return tiles;
    }

    /**
     * Sets the tile at the specified index.
     *
     * @param index The index of the tile.
     * @param tile  The TiledTile.
     */
    public void setTile(int index, TiledTile tile) {
        tiles.set(index, tile);
    }

    public TiledCustomProperties getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(TiledCustomProperties customProperties) {
        this.customProperties = customProperties;
    }

    public int getFirstGId() {
        return firstGId;
    }

    public void setFirstGId(int firstGId) {
        this.firstGId = firstGId;
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

    /**
     * Gets the ID of the tile frame from the tile's animation.
     *
     * @param gID       The tile's global ID.
     * @param currentMs The current time in milliseconds.
     * @return The ID of the tile frame.
     */
    private long getIDFromAnim(long gID, long currentMs) {
        TiledTile tile = tiles.get((int) gID);
        if (tile != null) {
            TiledAnimation animation = tile.getAnimation();

            if (animation != null) {
                return animation.getCurrentGID(currentMs, gID);
            }
        }

        return gID;
    }

    /**
     * Gets the tile's texture region.
     *
     * @param gID            The tile's global ID.
     * @param horizontalFlip Whether the tile is flipped horizontally.
     * @param verticalFlip   Whether the tile is flipped vertically.
     * @param currentMs      The current time in milliseconds.
     * @return The tile's texture region.
     */
    public Rectangle sourceAt(long gID, boolean horizontalFlip, boolean verticalFlip, long currentMs) {
        gID = gID - firstGId;

        if (gID >= tileCount) {
            return new Rectangle(0, 0, 0, 0);
        }

        gID = getIDFromAnim(gID, currentMs);

        float x = (gID % columns) * tileWidth;
        float y = (gID / columns) * tileHeight;
        float width = tileWidth, height = tileHeight;

        if (horizontalFlip) {
            x += tileWidth;
            width = -tileWidth;
        }

        if (verticalFlip) {
            y += tileHeight;
            height = -tileHeight;
        }

        return new Rectangle(x, y, width, height);
    }

    @Override
    public void dispose() {
        texture.dispose();
    }
}
