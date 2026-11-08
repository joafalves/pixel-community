package org.pixel.graphics.draw;

import java.util.Arrays;

/**
 * Frame-local, de-duplicated shape styles referenced by draw commands: identical styles get the same id, so a thousand
 * shapes with the same outline and shadow share one entry. Id 0 means "no style" (plain fill). Grows, never shrinks.
 * <p>
 * A style is a record of {@link #FLOATS} floats (8 vec4, the layout read by the shape shader):
 * <pre>
 *  0..3   stroke color (alpha 0 = no stroke)
 *  4      stroke width          5  stroke offset (signed, from the edge; negative = inside)
 *  6      dash length           7  dash gap            (dash length 0 = solid)
 *  8..11  gradient geometry, shape-local: linear (x0, y0, x1, y1), radial (cx, cy, radius, 0)
 * 12..15  gradient end color (the start color is the command's fill color)
 * 16..19  shadow color (alpha 0 = no shadow)
 * 20      shadow offset x      21  shadow offset y      22  shadow blur      23  shadow spread
 * 24      paint type ({@link #PAINT_SOLID}, {@link #PAINT_LINEAR}, {@link #PAINT_RADIAL})
 * 25      dash phase           26..27 reserved
 * 28..31  material data (the command's {@code Fragment.data}; see {@code Material})
 * </pre>
 */
public final class StyleTable {

    /**
     * Floats per style record (8 vec4).
     */
    public static final int FLOATS = 32;

    public static final int STROKE_COLOR = 0;
    public static final int STROKE_WIDTH = 4;
    public static final int STROKE_OFFSET = 5;
    public static final int DASH_LENGTH = 6;
    public static final int DASH_GAP = 7;
    public static final int GRADIENT_GEOMETRY = 8;
    public static final int GRADIENT_END_COLOR = 12;
    public static final int SHADOW_COLOR = 16;
    public static final int SHADOW_OFFSET_X = 20;
    public static final int SHADOW_OFFSET_Y = 21;
    public static final int SHADOW_BLUR = 22;
    public static final int SHADOW_SPREAD = 23;
    public static final int PAINT_TYPE = 24;
    public static final int DASH_PHASE = 25;
    public static final int MATERIAL_DATA = 28;

    public static final float PAINT_SOLID = 0f;
    public static final float PAINT_LINEAR = 1f;
    public static final float PAINT_RADIAL = 2f;

    private int size; // ids are 1..size
    private float[] records = new float[FLOATS * 32];
    private int[] hashSlots = new int[64]; // open addressing, stores ids (0 = empty)

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
     * Get the id of a style, adding it if it is new.
     *
     * @param record A style record ({@link #FLOATS} floats, see the class documentation). Copied.
     * @return The 1-based style id.
     */
    public int intern(float[] record) {
        int mask = hashSlots.length - 1;
        int slot = hash(record, 0) & mask;
        while (true) {
            int id = hashSlots[slot];
            if (id == 0) {
                break;
            }
            if (Arrays.equals(records, (id - 1) * FLOATS, id * FLOATS, record, 0, FLOATS)) {
                return id;
            }
            slot = (slot + 1) & mask;
        }

        if ((size + 1) * FLOATS > records.length) {
            records = Arrays.copyOf(records, records.length * 2);
        }
        System.arraycopy(record, 0, records, size * FLOATS, FLOATS);
        size++;
        hashSlots[slot] = size;
        if (size * 2 > hashSlots.length) {
            rehash(hashSlots.length * 2);
        }
        return size;
    }

    /**
     * @param id The 1-based style id.
     * @param k  The record index (0..{@link #FLOATS} - 1).
     * @return One value of the style record.
     */
    public float get(int id, int k) {
        return records[(id - 1) * FLOATS + k];
    }

    /**
     * Copy a style record.
     *
     * @param id     The 1-based style id.
     * @param dest   The destination array.
     * @param offset The destination offset.
     */
    public void copy(int id, float[] dest, int offset) {
        System.arraycopy(records, (id - 1) * FLOATS, dest, offset, FLOATS);
    }

    private void rehash(int capacity) {
        hashSlots = new int[capacity];
        int mask = capacity - 1;
        for (int i = 0; i < size; i++) {
            int slot = hash(records, i * FLOATS) & mask;
            while (hashSlots[slot] != 0) {
                slot = (slot + 1) & mask;
            }
            hashSlots[slot] = i + 1;
        }
    }

    private static int hash(float[] data, int offset) {
        int h = 1;
        for (int i = offset, end = offset + FLOATS; i < end; i++) {
            h = 31 * h + Float.floatToIntBits(data[i]);
        }
        return h ^ (h >>> 16);
    }
}
