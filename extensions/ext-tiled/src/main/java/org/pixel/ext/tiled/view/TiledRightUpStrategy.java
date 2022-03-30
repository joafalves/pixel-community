package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.TiledTileLayer;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;

/**
 * Draws the tile layer going right to left, bottom to up.
 */
public class TiledRightUpStrategy extends TiledDrawStrategy {
    @Override
    public void draw(SpriteBatch spriteBatch, Boundary boundary, TiledTileLayer layer, long currentMs) {
        for (int y = layer.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < layer.getWidth(); x++) {
                drawTile(spriteBatch, boundary, layer, x, y, currentMs);
            }
        }
    }
}
