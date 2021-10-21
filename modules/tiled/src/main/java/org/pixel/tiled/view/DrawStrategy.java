package org.pixel.tiled.view;

import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;
import org.pixel.tiled.content.TileLayer;
import org.pixel.tiled.content.TileSet;

import java.util.List;
import java.util.ListIterator;

import static org.pixel.tiled.content.TiledConstants.*;

public abstract class DrawStrategy {
    private static final Transform transform = new Transform();
    private static final Vector2 position = new Vector2();

    private void getTileTransform(long gID, TileSet tileSet) {
        boolean horizontalFlip = (gID & HORIZONTAL_FLIP_FLAG.getBits()) != 0;
        boolean verticalFlip = (gID & VERTICAL_FLIP_FLAG.getBits()) != 0;
        boolean diagonalFlip = (gID & DIAGONAL_FLIP_FLAG.getBits()) != 0;

        transform.rotation = 0f;
        gID &= ~(HORIZONTAL_FLIP_FLAG.getBits() | VERTICAL_FLIP_FLAG.getBits() | DIAGONAL_FLIP_FLAG.getBits());

        if (diagonalFlip) {
            transform.rotation = (float) Math.PI / 2;
            transform.rectangle = tileSet.sourceAt(gID, !verticalFlip, horizontalFlip);
        } else {
            transform.rectangle = tileSet.sourceAt(gID, horizontalFlip, verticalFlip);
        }
    }

    protected void drawTile(SpriteBatch spriteBatch, TileLayer layer, int x, int y) {
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
                position.setX(x * layer.getTileMap().getTileWidth() + (float) layer.getOffsetX() + 0.5f * tileSet.getTileWidth());
                position.setY(y * layer.getTileMap().getTileHeight() + (float) layer.getOffsetY() + 0.5f * tileSet.getTileHeight());

                getTileTransform(originalGID, tileSet);

                spriteBatch.draw(tileSet.getTexture(), position, transform.rectangle, Color.WHITE, Vector2.HALF,
                        1f, 1f, transform.rotation);

                break;
            }
        }
    }

    public abstract void draw(SpriteBatch spriteBatch, TileLayer layer);

    private static class Transform {
        private Rectangle rectangle = null;
        private float rotation;

        private Transform() {
            rotation = 0f;
        }
    }
}
