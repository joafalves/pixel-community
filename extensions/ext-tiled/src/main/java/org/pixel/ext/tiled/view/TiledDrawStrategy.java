package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.TiledTileLayer;
import org.pixel.ext.tiled.content.TiledTileSet;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.util.List;
import java.util.ListIterator;

import static org.pixel.ext.tiled.content.TiledConstants.PIXEL_EPSILON;
import static org.pixel.ext.tiled.content.TiledFlipMasks.*;

/**
 * A strategy for drawing a TiledTileLayer.
 */
public abstract class TiledDrawStrategy {
    private final Transform transform = new Transform();
    private final Vector2 position = new Vector2();
    private final Boundary tileBoundary = new Boundary(0, 0, 0, 0);

    private void getTileTransform(long gID, TiledTileSet tileSet, long frame) {
        boolean horizontalFlip = (gID & HORIZONTAL_FLIP_FLAG.getBits()) != 0;
        boolean verticalFlip = (gID & VERTICAL_FLIP_FLAG.getBits()) != 0;
        boolean diagonalFlip = (gID & DIAGONAL_FLIP_FLAG.getBits()) != 0;

        transform.rotation = 0f;
        gID &= ~(HORIZONTAL_FLIP_FLAG.getBits() | VERTICAL_FLIP_FLAG.getBits() | DIAGONAL_FLIP_FLAG.getBits());

        if (diagonalFlip) {
            transform.rotation = -(float) Math.PI / 2;
            transform.rectangle = tileSet.sourceAt(gID, !verticalFlip, horizontalFlip, frame);
        } else {
            transform.rectangle = tileSet.sourceAt(gID, horizontalFlip, verticalFlip, frame);
        }
    }

    protected void drawTile(SpriteBatch spriteBatch, Boundary boundary, TiledTileLayer layer, int x, int y, long frame) {
        List<TiledTileSet> tileSets = layer.getTileMap().getTileSets();
        long gID = layer.getTiles()[y][x];
        long originalGID = gID;
        ListIterator<TiledTileSet> itr = tileSets.listIterator(tileSets.size());

        gID &= ~(HORIZONTAL_FLIP_FLAG.getBits() | VERTICAL_FLIP_FLAG.getBits() | DIAGONAL_FLIP_FLAG.getBits());

        if (gID == 0) {
            return;
        }

        while (itr.hasPrevious()) {
            TiledTileSet tileSet = itr.previous();

            if (tileSet.getFirstGId() <= gID) {
                position.setX((float) Math.floor(x * layer.getTileMap().getTileWidth() + layer.getOffsetX() + 0.5 * tileSet.getTileWidth() + PIXEL_EPSILON.getValue()));
                position.setY((float) Math.floor(y * layer.getTileMap().getTileHeight() + layer.getOffsetY() + 0.5 * tileSet.getTileHeight() + PIXEL_EPSILON.getValue()));

                tileBoundary.set(
                        position.getX() - 0.5f * tileSet.getTileWidth(),
                        position.getY() - 0.5f * tileSet.getTileHeight(),
                        tileSet.getTileWidth(), tileSet.getTileHeight()
                );

                if (!boundary.overlaps(tileBoundary)) {
                    continue;
                }

                getTileTransform(originalGID, tileSet, frame);

                spriteBatch.draw(tileSet.getTexture(), position, transform.rectangle, Color.WHITE, Vector2.HALF,
                        1f, 1f, transform.rotation);

                break;
            }
        }
    }

    /**
     * Draws a TiledTileLayer.
     *
     * @param spriteBatch The SpriteBatch to draw with.
     * @param boundary    The boundary to draw within.
     * @param layer       The layer to draw.
     * @param currentMs   The current time in milliseconds.
     */
    public abstract void draw(SpriteBatch spriteBatch, Boundary boundary, TiledTileLayer layer, long currentMs);

    private static class Transform {
        private Rectangle rectangle = null;
        private float rotation;

        private Transform() {
            rotation = 0f;
        }
    }
}
