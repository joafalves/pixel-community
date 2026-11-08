package org.pixel.demo.concept.spaceshooter.assets;

import org.pixel.content.Texture;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.math.Rectangle;

/**
 * Pixel-art drawing: sprites land on whole virtual pixels, so nearest-filtered art stays crisp at any integer window
 * scale.
 */
public final class Sprites {

    private Sprites() {
    }

    /**
     * Draw a sprite-map region centered on (x, y).
     */
    public static Canvas.Image draw(Canvas canvas, Texture texture, Rectangle region, float x, float y,
                                    boolean mirror) {
        return draw(canvas, texture, region.getX(), region.getY(), region.getWidth(), region.getHeight(), x, y, 1f,
                mirror);
    }

    /**
     * Draw part of a texture centered on (x, y) at an integer scale, its top-left corner on a whole pixel. Mirroring
     * (the art faces right; enemies face left) swaps the texture coordinates instead of transforming the canvas, so
     * the returned command stays open for a tint, a material or a depth.
     */
    public static Canvas.Image draw(Canvas canvas, Texture texture, float sx, float sy, float sw, float sh,
                                    float x, float y, float scale, boolean mirror) {
        float w = sw * scale, h = sh * scale;
        Canvas.Image image = canvas.image(texture, Math.round(x - w * 0.5f), Math.round(y - h * 0.5f));
        if (mirror) {
            image.source(sx + sw, sy, -sw, sh);
        } else {
            image.source(sx, sy, sw, sh);
        }
        return image.size(w, h);
    }
}
