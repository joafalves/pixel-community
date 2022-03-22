package org.pixel.ext.tiled.view;

import org.pixel.ext.tiled.content.TileLayer;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;

public class RightDownStrategy extends DrawStrategy {
    @Override
    public void draw(SpriteBatch spriteBatch, Boundary boundary, TileLayer layer, long frame) {
        for (int y = 0; y < layer.getHeight(); y++) {
            for (int x = 0; x < layer.getWidth(); x++) {
                drawTile(spriteBatch, boundary, layer, x, y, frame);
            }
        }
    }
}
