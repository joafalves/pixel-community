package org.pixel.graphics.text;

import java.util.Arrays;

/**
 * Packs glyph rectangles into a power-of-two atlas with a shelf algorithm (tallest first). GL-free.
 */
public final class GlyphPacker {

    private final int spacing;
    private int count;
    private int[] widths = new int[128];
    private int[] heights = new int[128];
    private int[] xs = new int[128];
    private int[] ys = new int[128];
    private int atlasWidth;
    private int atlasHeight;

    /**
     * @param spacing Empty pixels between rectangles (and around the atlas border).
     */
    public GlyphPacker(int spacing) {
        this.spacing = Math.max(0, spacing);
    }

    /**
     * Add a rectangle.
     *
     * @return Its index.
     */
    public int add(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Rectangle size must be positive.");
        }
        if (count == widths.length) {
            widths = Arrays.copyOf(widths, count * 2);
            heights = Arrays.copyOf(heights, count * 2);
            xs = Arrays.copyOf(xs, count * 2);
            ys = Arrays.copyOf(ys, count * 2);
        }
        widths[count] = width;
        heights[count] = height;
        return count++;
    }

    /**
     * Place every rectangle, growing the atlas (powers of two, at most {@code maxSize}) until they fit.
     *
     * @param maxSize Largest allowed atlas side.
     * @throws IllegalStateException when the rectangles don't fit in {@code maxSize x maxSize}.
     */
    public void pack(int maxSize) {
        long area = 0;
        int widest = 1;
        for (int i = 0; i < count; i++) {
            area += (long) (widths[i] + spacing) * (heights[i] + spacing);
            widest = Math.max(widest, widths[i] + 2 * spacing);
        }
        int width = nextPowerOfTwo(Math.max(widest, (int) Math.ceil(Math.sqrt(area))));
        int height = width / 2;
        Integer[] order = new Integer[count];
        for (int i = 0; i < count; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (a, b) -> heights[b] != heights[a] ? heights[b] - heights[a] : widths[b] - widths[a]);
        while (true) {
            if (width > maxSize) {
                throw new IllegalStateException("Glyphs don't fit in a " + maxSize + "x" + maxSize
                        + " atlas; reduce the glyph size or the character set.");
            }
            if (height < 1) {
                height = 1;
            }
            if (place(order, width, height)) {
                atlasWidth = width;
                atlasHeight = height;
                return;
            }
            if (height < width) {
                height *= 2;
            } else {
                width *= 2;
            }
        }
    }

    private boolean place(Integer[] order, int width, int height) {
        int x = spacing, y = spacing, shelf = 0;
        for (Integer i : order) {
            if (x + widths[i] + spacing > width) {
                x = spacing;
                y += shelf + spacing;
                shelf = 0;
            }
            if (y + heights[i] + spacing > height) {
                return false;
            }
            xs[i] = x;
            ys[i] = y;
            x += widths[i] + spacing;
            shelf = Math.max(shelf, heights[i]);
        }
        return true;
    }

    private static int nextPowerOfTwo(int v) {
        int p = 1;
        while (p < v) {
            p <<= 1;
        }
        return p;
    }

    public int x(int index) {
        return xs[index];
    }

    public int y(int index) {
        return ys[index];
    }

    public int getAtlasWidth() {
        return atlasWidth;
    }

    public int getAtlasHeight() {
        return atlasHeight;
    }
}
