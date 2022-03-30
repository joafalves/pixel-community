package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.TiledImageLayer;
import org.pixel.graphics.Color;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;
import org.pixel.math.Vector2;

/**
 * A view for a tiled image layer.
 */
public class TiledImageLayerView implements TiledView<TiledImageLayer> {
    private final Boundary imageBoundary;
    private final Vector2 position;
    SpriteBatch spriteBatch;
    Boundary boundary;

    /**
     * Creates a new tiled image layer view.
     * @param spriteBatch  The sprite batch to use.
     * @param boundary The boundary to draw within.
     */
    public TiledImageLayerView(SpriteBatch spriteBatch, Boundary boundary) {
        this.spriteBatch = spriteBatch;
        this.boundary = boundary;

        imageBoundary = new Boundary(0, 0, 0, 0);
        position = new Vector2(0, 0);
    }

    /**
     * Draws the layer.
     * @param layer The layer to draw.
     * @param currentMs The current time in milliseconds.
     */
    @Override
    public void draw(TiledImageLayer layer, long currentMs) {
        position.set((float) layer.getOffsetX(), (float) layer.getOffsetY());

        if(layer.getImage() == null) {
            return;
        }

        imageBoundary.set(position.getX(), position.getY(), layer.getImage().getWidth(), layer.getImage().getHeight());

        if (!boundary.overlaps(imageBoundary)) {
            return;
        }

        spriteBatch.draw(layer.getImage(), position, Color.WHITE, Vector2.ZERO);
    }
}
