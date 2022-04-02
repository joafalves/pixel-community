package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.TiledObject;
import org.pixel.ext.tiled.content.TiledObjectGroup;
import org.pixel.ext.tiled.content.TiledTileObject;
import org.pixel.ext.tiled.content.TiledTileSet;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.util.List;
import java.util.ListIterator;

import static org.pixel.ext.tiled.content.TiledFlipMasks.HORIZONTAL_FLIP_FLAG;
import static org.pixel.ext.tiled.content.TiledFlipMasks.VERTICAL_FLIP_FLAG;

/**
 * A view for a TiledObjectGroup.
 */
public class TiledObjectGroupView implements TiledGenericObjectGroupView {
    private final Vector2 position = new Vector2();
    private final SpriteBatch spriteBatch;
    private final Boundary boundary;
    private final Boundary tileBoundary;
    private long frame;

    /**
     * Creates a new TiledObjectGroupView.
     *
     * @param spriteBatch The SpriteBatch to use.
     * @param boundary    The boundary to draw within.
     */
    public TiledObjectGroupView(SpriteBatch spriteBatch, Boundary boundary) {
        this.spriteBatch = spriteBatch;
        this.boundary = boundary;
        this.tileBoundary = new Boundary(0, 0, 0, 0);
    }

    /**
     * Draws a TiledTileObject.
     *
     * @param tile  The TiledTileObject to draw.
     * @param group The TiledObjectGroup that contains the object.
     */
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
                tileBoundary.rotate(new Vector2(position.getX(), position.getY()), tile.getRotation());

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

    /**
     * Draws a TiledObjectGroup.
     *
     * @param element   The TiledObjectGroup to draw.
     * @param currentMs The current time in milliseconds.
     */
    @Override
    public void draw(TiledObjectGroup element, long currentMs) {
        this.frame = currentMs;

        for (TiledObject object : element.getObjects().values()) {
            object.draw(element, this);
        }
    }
}
