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

    private Transform getTileTransform(long gID) {
        boolean horizontalFlip = (gID & HORIZONTAL_FLIP_FLAG.getBits()) != 0;
        boolean verticalFlip = (gID & VERTICAL_FLIP_FLAG.getBits()) != 0;
        boolean diagonalFlip = (gID & DIAGONAL_FLIP_FLAG.getBits()) != 0;
        Transform transform = new Transform();

        // diagonal flip in tiled is done before the other flips, since scaling is done before rotation
        // that isn't
        // possible these are some workarounds
        if (diagonalFlip) {
            transform.setRotation((float) Math.PI / 2);
            transform.setScaleX(verticalFlip ? 1f : -1f);
            transform.setScaleY(horizontalFlip ? -1f : 1f);
        } else {
            transform.setScaleX(horizontalFlip ? -1f : 1f);
            transform.setScaleY(verticalFlip ? -1f : 1f);
        }

        return transform;
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

                Vector2 position = new Vector2(x * layer.getTileMap().getTileWidth() + (float) layer.getOffsetX() + 0.5f * tileSet.getTileWidth(),
                        y * layer.getTileMap().getTileHeight() + (float) layer.getOffsetY() + 0.5f * tileSet.getTileHeight());

                Transform transform = getTileTransform(originalGID);

                spriteBatch.draw(tileSet.getTexture(), position, source, Color.WHITE, Vector2.HALF,
                        transform.getScaleX(), transform.getScaleY(), transform.getRotation());

                break;
            }
        }
    }

    public abstract void draw(SpriteBatch spriteBatch, Layer layer);

}
