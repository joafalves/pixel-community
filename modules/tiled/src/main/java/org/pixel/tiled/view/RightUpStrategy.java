package org.pixel.tiled.view;

import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Boundary;
import org.pixel.tiled.content.TileLayer;

public class RightUpStrategy extends DrawStrategy {
    @Override
    public void draw(SpriteBatch spriteBatch, Boundary boundary, TileLayer layer, long frame) {
        for (int y = layer.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < layer.getWidth(); x++) {
                drawTile(spriteBatch, boundary, layer, x, y, frame);
            }
        }
    }
}
