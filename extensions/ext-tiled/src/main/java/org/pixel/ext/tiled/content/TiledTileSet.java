package org.pixel.ext.tiled.content;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.content.Texture;
import org.pixel.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class TiledTileSet implements Disposable {
    private final int tileWidth;
    private final int tileHeight;
    private final int tileCount;
    private final int columns;
    private final Texture texture;
    private final List<TiledTile> tiles;
    private int firstGId;
    private TiledCustomProperties customProperties;

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

    public List<TiledTile> getTiles() {
        return tiles;
    }

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

    private long getIDFromAnim(long gID, long frame) {
        TiledTile tile = tiles.get((int) gID);
        if (tile != null) {
            TiledAnimation animation = tile.getAnimation();

            if (animation != null) {
                long currentFrame = frame % animation.getTotalFrameCount();
                currentFrame = Math.abs(currentFrame);
                long frameSum = 0;

                for (TiledFrame animFrame : animation.getFrameList()) {
                    frameSum += animFrame.getDuration();

                    if (frameSum > currentFrame) {
                        gID = animFrame.getLocalId();

                        break;
                    }
                }
            }
        }

        return gID;
    }

    public Rectangle sourceAt(long gID, boolean horizontalFlip, boolean verticalFlip, long frame) {
        gID = gID - firstGId;

        if (gID >= tileCount) {
            return new Rectangle(0, 0, 0, 0);
        }

        gID = getIDFromAnim(gID, frame);

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
