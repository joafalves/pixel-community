package org.pixel.tiled.view;

import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;
import org.pixel.tiled.content.Layer;
import org.pixel.tiled.content.TileSet;

import java.util.List;
import java.util.ListIterator;

public abstract class DrawStrategy {
    private static final long HORIZONTAL_FLIP_FLAG = 0x80000000;
    private static final long VERTICAL_FLIP_FLAG = 0x40000000;
    private static final long DIAGONAL_FLIP_FLAG = 0x20000000;

    private static class Transform {
        private float scaleX, scaleY;
        private float rotation;

        private Transform() {
            scaleX = scaleY = 1f;
            rotation = 0f;
        }
    }

    private Transform getTileTransform(long gID) {
        boolean horizontalFlip = (gID & HORIZONTAL_FLIP_FLAG) > 0;
        boolean verticalFlip = (gID & VERTICAL_FLIP_FLAG) > 0;
        boolean diagonalFlip = (gID & DIAGONAL_FLIP_FLAG) > 0;
        Transform transform = new Transform();

        // diagonal flip in tiled is done before the other flips, since scaling is done before rotation
        // that isn't
        // possible these are some workarounds
        if(diagonalFlip) {
            transform.rotation = (float) Math.PI / 2;
            transform.scaleX = -1f;

            if(horizontalFlip) {
                transform.scaleY = -1f;
            }
            if(verticalFlip) {
                transform.scaleX = 1f;
            }
        } else {
            if(horizontalFlip) {
                transform.scaleX = -1f;
            }
            if(verticalFlip) {
                transform.scaleY = -1f;
            }
        }

        return transform;
    }

    protected void drawTile(SpriteBatch spriteBatch, Layer layer, int x, int y) {
        List<TileSet> tileSets = layer.getTileMap().getTileSets();
        long gID = layer.getTiles()[y][x];
        long originalGID = gID;
        ListIterator<TileSet> itr = tileSets.listIterator(tileSets.size());

        gID &= ~(HORIZONTAL_FLIP_FLAG | VERTICAL_FLIP_FLAG | DIAGONAL_FLIP_FLAG);

        if(gID == 0) {
            return;
        }

        while(itr.hasPrevious()) {
            TileSet tileSet = itr.previous();

            if(tileSet.getFirstGId() <= gID) {
                Rectangle source = tileSet.sourceAt(gID - tileSet.getFirstGId());

                Vector2 position = new Vector2(x * layer.getTileMap().getTileWidth() + (float)layer.getOffsetX(),
                        y * layer.getTileMap().getTileHeight() + (float)layer.getOffsetY());

                Transform transform = getTileTransform(originalGID);

                spriteBatch.draw(tileSet.getTexture(), position, source, Color.WHITE, Vector2.HALF,
                        transform.scaleX, transform.scaleY, transform.rotation);

                break;
            }
        }
    }

    public abstract void draw(SpriteBatch spriteBatch, Layer layer);
}
