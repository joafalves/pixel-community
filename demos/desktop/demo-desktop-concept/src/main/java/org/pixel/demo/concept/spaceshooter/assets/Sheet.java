package org.pixel.demo.concept.spaceshooter.assets;

import org.pixel.content.Texture;
import org.pixel.graphics.canvas.Canvas;

/**
 * An animation laid out on a grid of equal cells, row by row, and the frames to play.
 */
public final class Sheet {

    public final Texture texture;
    public final int columns, cellWidth, cellHeight;
    /** The frames that hold the animation (sheets often start or end with empty cells). */
    public final int first, last;

    public Sheet(Texture texture, int cellWidth, int cellHeight, int first, int last) {
        this.texture = texture;
        this.columns = texture.getWidth() / cellWidth;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.first = first;
        this.last = last;
    }

    public int frameCount() {
        return last - first + 1;
    }

    /**
     * The frame at a time, playing at {@code fps}: looping, or held on the last frame.
     */
    public int frameAt(float seconds, float fps, boolean loop) {
        int index = (int) (seconds * fps);
        if (loop) {
            index %= frameCount();
        } else {
            index = Math.min(index, frameCount() - 1);
        }
        return first + Math.max(0, index);
    }

    /**
     * Draw a frame centered on (x, y) (see {@link Sprites#draw}).
     */
    public Canvas.Image draw(Canvas canvas, int frame, float x, float y, float scale, boolean mirror) {
        int sx = (frame % columns) * cellWidth, sy = (frame / columns) * cellHeight;
        return Sprites.draw(canvas, texture, sx, sy, cellWidth, cellHeight, x, y, scale, mirror);
    }
}
