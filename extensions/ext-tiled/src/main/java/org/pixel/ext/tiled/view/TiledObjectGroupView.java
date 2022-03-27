package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.TiledTileSet;
import org.pixel.ext.tiled.content.TiledObject;
import org.pixel.ext.tiled.content.TiledObjectGroup;
import org.pixel.ext.tiled.content.TiledTileObject;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.util.List;
import java.util.ListIterator;

import static org.pixel.ext.tiled.content.TiledFlipMasks.HORIZONTAL_FLIP_FLAG;
import static org.pixel.ext.tiled.content.TiledFlipMasks.VERTICAL_FLIP_FLAG;

public class TiledObjectGroupView implements TiledGenericObjectGroupView {
    private final Vector2 position = new Vector2();
    private final SpriteBatch spriteBatch;
    private final Boundary boundary;
    private final Boundary tileBoundary;
    private long frame;

    public TiledObjectGroupView(SpriteBatch spriteBatch, Boundary boundary) {
        this.spriteBatch = spriteBatch;
        this.boundary = boundary;
        this.tileBoundary = new Boundary(0, 0, 0, 0);
    }

    @Override
    public void draw(TiledTileObject tile, TiledObjectGroup group) {
        List<TiledTileSet> tileSets = group.getTileMap().getTileSets();
        long gID = tile.getgID();
        ListIterator<TiledTileSet> itr = tileSets.listIterator(tileSets.size());

        boolean horizontalFlip = (gID & HORIZONTAL_FLIP_FLAG.getBits()) != 0;
        boolean verticalFlip = (gID & VERTICAL_FLIP_FLAG.getBits()) != 0;

        gID &= ~(HORIZONTAL_FLIP_FLAG.getBits() | VERTICAL_FLIP_FLAG.getBits());

        while (itr.hasPrevious()) {
            TiledTileSet tileSet = itr.previous();

            if (tileSet.getFirstGId() <= gID) {
                Rectangle source = tileSet.sourceAt(gID, horizontalFlip, verticalFlip, frame);

                position.setY(tile.getPosition().getY() + (float) group.getOffsetY());
                position.setX(tile.getPosition().getX() + (float) group.getOffsetX());

                tileBoundary.set(position.getX(), (float) (position.getY() - tile.getHeight()), (float) tile.getWidth(), (float) tile.getHeight());
//                spriteBatch.draw(
//                        tileSet.getTexture(), position, source, Color.WHITE, Vector2.ZERO_ONE,
//                        (float) tile.getWidth() / tileSet.getTileWidth(),
//                        (float) tile.getHeight() / tileSet.getTileHeight(),
//                        0f
//                );

                if (!boundary.overlaps(tileBoundary)) {
                    continue;
                }

                spriteBatch.draw(
                        tileSet.getTexture(), position, source, Color.WHITE, Vector2.ZERO_ONE,
                        (float) tile.getWidth() / tileSet.getTileWidth(),
                        (float) tile.getHeight() / tileSet.getTileHeight(),
                        (tile.getRotation())
                );

                break;
            }
        }
    }

    @Override
    public void draw(TiledObjectGroup element, long frame) {
        this.frame = frame;

        for (TiledObject object : element.getObjects().values()) {
            object.draw(element, this);
        }
    }
}