package org.pixel.graphics.draw;

import java.util.Arrays;

/**
 * Frame-local clip rectangles (axis-aligned, layer space) referenced by draw commands. Id 0 means "no clip".
 * Front-ends add a rectangle when the clip changes and reuse its id while it stays the same. Grows, never shrinks.
 */
public final class ClipTable {

    private int size; // ids are 1..size
    private float[] rects = new float[4 * 32];

    /**
     * Remove all clips (capacity is kept).
     */
    public void clear() {
        size = 0;
    }

    public int size() {
        return size;
    }

    /**
     * Add a clip rectangle.
     *
     * @param x0 Left.
     * @param y0 Top.
     * @param x1 Right (exclusive).
     * @param y1 Bottom (exclusive).
     * @return The 1-based clip id.
     */
    public int add(float x0, float y0, float x1, float y1) {
        if ((size + 1) * 4 > rects.length) {
            rects = Arrays.copyOf(rects, rects.length * 2);
        }
        int i = size * 4;
        rects[i] = x0;
        rects[i + 1] = y0;
        rects[i + 2] = x1;
        rects[i + 3] = y1;
        return ++size;
    }

    public float x0(int id) {
        return rects[(id - 1) * 4];
    }

    public float y0(int id) {
        return rects[(id - 1) * 4 + 1];
    }

    public float x1(int id) {
        return rects[(id - 1) * 4 + 2];
    }

    public float y1(int id) {
        return rects[(id - 1) * 4 + 3];
    }
}
