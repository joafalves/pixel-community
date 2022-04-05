package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.TiledTileLayer;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;

/**
 * Draws the tile layer going left to right, top to bottom.
 */
public class TiledLeftDownStrategy extends TiledDrawStrategy {
    @Override
    public void draw(SpriteBatch spriteBatch, Boundary boundary, TiledTileLayer layer, long currentMs) {
        for (int y = 0; y < layer.getHeight(); y++) {
            for (int x = layer.getWidth() - 1; x >= 0; x--) {
                drawTile(spriteBatch, boundary, layer, x, y, currentMs);
            }
        }
    }
}
