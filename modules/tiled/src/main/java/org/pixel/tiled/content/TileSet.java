package org.pixel.tiled.content;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.content.Texture;
import org.pixel.math.Rectangle;

public class TileSet implements Disposable {
    private final int tileWidth;
    private final int tileHeight;
    private final int tileCount;
    private final int columns;
    private final Texture texture;
    private int firstGId;
    private TiledCustomProperties customProperties;

    public TileSet(int tileWidth, int tileHeight, int tileCount, int columns, Texture texture) {
        this.tileCount = tileCount;
        this.tileHeight = tileHeight;
        this.tileWidth = tileWidth;
        this.columns = columns;
        this.texture = texture;
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

    public Rectangle sourceAt(long gID) {
        gID = gID - firstGId;

        if (gID >= tileCount) {
            return new Rectangle(0, 0, 0, 0);
        }

        float x = (gID % columns) * tileWidth;
        float y = (gID / columns) * tileHeight;

        return new Rectangle(x, y, tileWidth, tileHeight);
    }

    public Rectangle sourceAt(long gID, boolean horizontalFlip, boolean verticalFlip) {
        gID = gID - firstGId;

        if (gID >= tileCount) {
            return new Rectangle(0, 0, 0, 0);
        }

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
