package org.pixel.tiled.view;

import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;
import org.pixel.tiled.content.TileSet;
import org.pixel.tiled.content.TiledObject;
import org.pixel.tiled.content.TiledObjectGroup;
import org.pixel.tiled.content.TiledTileObject;

import java.util.List;
import java.util.ListIterator;

import static org.pixel.tiled.content.TiledConstants.HORIZONTAL_FLIP_FLAG;
import static org.pixel.tiled.content.TiledConstants.VERTICAL_FLIP_FLAG;

public class TiledObjectGroupView implements TiledView<TiledObjectGroup> {
    private static final Vector2 position = new Vector2();

    public void draw(SpriteBatch spriteBatch, TiledTileObject tile, TiledObjectGroup group) {
        List<TileSet> tileSets = group.getTileMap().getTileSets();
        long gID = tile.getgID();
        ListIterator<TileSet> itr = tileSets.listIterator(tileSets.size());

        boolean horizontalFlip = (gID & HORIZONTAL_FLIP_FLAG.getBits()) != 0;
        boolean verticalFlip = (gID & VERTICAL_FLIP_FLAG.getBits()) != 0;

        gID &= ~(HORIZONTAL_FLIP_FLAG.getBits() | VERTICAL_FLIP_FLAG.getBits());

        while (itr.hasPrevious()) {
            TileSet tileSet = itr.previous();

            if (tileSet.getFirstGId() <= gID) {
                Rectangle source = tileSet.sourceAt(gID, horizontalFlip, verticalFlip);

                position.setY(tile.getPosition().getY() + (float) group.getOffsetY());
                position.setX(tile.getPosition().getX() + (float) group.getOffsetX());

                spriteBatch.draw(
                        tileSet.getTexture(), position, source, Color.WHITE, Vector2.ZERO_ONE,
                        (float) tile.getWidth() / tileSet.getTileWidth(),
                        (float) tile.getHeight() / tileSet.getTileHeight(),
                        -(tile.getRotation())
                );

                break;
            }
        }
    }

    @Override
    public void draw(SpriteBatch spriteBatch, TiledObjectGroup element) {
        for (TiledObject object : element.getObjects().values()) {
            object.draw(spriteBatch, element, this);
        }
    }
}
