package org.pixel.tiled.view;

import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;
import org.pixel.tiled.content.Layer;
import org.pixel.tiled.content.TileSet;

import java.util.List;
import java.util.ListIterator;

import static org.pixel.tiled.content.TiledConstants.*;

public abstract class DrawStrategy {
    private static final Transform transform = new Transform();
    private static final Vector2 position = new Vector2();

    private void getTileTransform(long gID) {
        boolean horizontalFlip = (gID & HORIZONTAL_FLIP_FLAG.getBits()) != 0;
        boolean verticalFlip = (gID & VERTICAL_FLIP_FLAG.getBits()) != 0;
        boolean diagonalFlip = (gID & DIAGONAL_FLIP_FLAG.getBits()) != 0;

        transform.rotation = 0f;

        if (diagonalFlip) {
            transform.rotation = (float) Math.PI / 2;
            transform.scaleX = verticalFlip ? 1f : -1f;
            transform.scaleY = horizontalFlip ? -1f : 1f;
        } else {
            transform.scaleX = horizontalFlip ? -1f : 1f;
            transform.scaleY = verticalFlip ? -1f : 1f;
        }
    }

    protected void drawTile(SpriteBatch spriteBatch, Layer layer, int x, int y) {
        List<TileSet> tileSets = layer.getTileMap().getTileSets();
        long gID = layer.getTiles()[y][x];
        long originalGID = gID;
        ListIterator<TileSet> itr = tileSets.listIterator(tileSets.size());

        gID &= ~(HORIZONTAL_FLIP_FLAG.getBits() | VERTICAL_FLIP_FLAG.getBits() | DIAGONAL_FLIP_FLAG.getBits());

        if (gID == 0) {
            return;
        }

        while (itr.hasPrevious()) {
            TileSet tileSet = itr.previous();

            if (tileSet.getFirstGId() <= gID) {
                Rectangle source = tileSet.sourceAt(gID);

                position.setX(x * layer.getTileMap().getTileWidth() + (float) layer.getOffsetX() + 0.5f * tileSet.getTileWidth());
                position.setY(y * layer.getTileMap().getTileHeight() + (float) layer.getOffsetY() + 0.5f * tileSet.getTileHeight());

                getTileTransform(originalGID);

                spriteBatch.draw(tileSet.getTexture(), position, source, Color.WHITE, Vector2.HALF,
                        transform.scaleX, transform.scaleY, transform.rotation);

                break;
            }
        }
    }

    public abstract void draw(SpriteBatch spriteBatch, Layer layer);

    private static class Transform {
        private float scaleX;
        private float scaleY;
        private float rotation;

        Transform() {
            this.scaleX = this.scaleY = 1f;
            this.rotation = 0f;
        }
    }
}
