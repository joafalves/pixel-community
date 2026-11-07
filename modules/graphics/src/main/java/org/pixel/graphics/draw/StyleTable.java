package org.pixel.graphics.draw;

import java.util.Arrays;

/**
 * Frame-local, de-duplicated stroke styles referenced by draw commands. Identical styles get the same id, so a
 * thousand outlined shapes with the same stroke share one entry. Id 0 means "no stroke". Grows, never shrinks.
 */
public final class StyleTable {

    private int size; // ids are 1..size
    private int[] rgba = new int[64];
    private float[] width = new float[64];
    private float[] offset = new float[64];
    private int[] hashSlots = new int[128]; // open addressing, stores ids (0 = empty)

    /**
     * Remove all styles (capacity is kept).
     */
    public void clear() {
        if (size > 0) {
            Arrays.fill(hashSlots, 0);
            size = 0;
        }
    }

    /**
     * @return The number of distinct styles.
     */
    public int size() {
        return size;
    }

    /**
     * Get the id of a stroke style, adding it if it is new.
     *
     * @param strokeRgba   Packed stroke color.
     * @param strokeWidth  Stroke width (local units).
     * @param strokeOffset Signed offset of the stroke band's center from the shape edge (negative = inside).
     * @return The 1-based style id.
     */
    public int intern(int strokeRgba, float strokeWidth, float strokeOffset) {
        int mask = hashSlots.length - 1;
        int slot = hash(strokeRgba, strokeWidth, strokeOffset) & mask;
        while (true) {
            int id = hashSlots[slot];
            if (id == 0) {
                break;
            }
            int i = id - 1;
            if (rgba[i] == strokeRgba && width[i] == strokeWidth && offset[i] == strokeOffset) {
                return id;
            }
            slot = (slot + 1) & mask;
        }

        if (size == rgba.length) {
            rgba = Arrays.copyOf(rgba, size * 2);
            width = Arrays.copyOf(width, size * 2);
            offset = Arrays.copyOf(offset, size * 2);
        }
        rgba[size] = strokeRgba;
        width[size] = strokeWidth;
        offset[size] = strokeOffset;
        size++;
        hashSlots[slot] = size;
        if (size * 2 > hashSlots.length) {
            rehash(hashSlots.length * 2);
        }
        return size;
    }

    public int rgba(int id) {
        return rgba[id - 1];
    }

    public float width(int id) {
        return width[id - 1];
    }

    public float offset(int id) {
        return offset[id - 1];
    }

    private void rehash(int capacity) {
        hashSlots = new int[capacity];
        int mask = capacity - 1;
        for (int i = 0; i < size; i++) {
            int slot = hash(rgba[i], width[i], offset[i]) & mask;
            while (hashSlots[slot] != 0) {
                slot = (slot + 1) & mask;
            }
            hashSlots[slot] = i + 1;
        }
    }

    private static int hash(int color, float w, float o) {
        int h = color * 31 + Float.floatToIntBits(w);
        h = h * 31 + Float.floatToIntBits(o);
        return h ^ (h >>> 16);
    }
}
