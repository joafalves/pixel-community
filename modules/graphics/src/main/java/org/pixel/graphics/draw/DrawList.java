package org.pixel.graphics.draw;

import org.pixel.content.Texture;

import java.util.Arrays;

/**
 * GL-free, allocation-free storage of the draw commands of one layer for one frame.
 * <p>
 * Commands are textured quads stored struct-of-arrays in primitive arrays that grow but never shrink. After
 * {@link #prepare()}, {@link #orderedIndex(int)} gives the commands in final draw order (see {@link LayerMode}).
 * Front-ends (SpriteBatch, Canvas, ...) record here; a {@link Batcher} turns the result into GPU batches.
 */
public final class DrawList {

    private static final int INITIAL_CAPACITY = 1024;

    private final LayerMode mode;

    private int size;
    private Texture[] textures;
    private float[] positions; // 8 per quad: x,y of top-left, top-right, bottom-right, bottom-left
    private float[] texCoords; // 4 per quad: u0, v0, u1, v1
    private int[] colors;      // packed RGBA8 (see Colors)
    private long[] keys;       // SORTED: depth then sequence
    private long[] sortBuffer; // radix sort scratch (same capacity as keys)
    private final int[] radixCounts = new int[256];
    private boolean needsSort;
    private float firstDepth;
    private boolean prepared;

    /**
     * Constructor.
     *
     * @param mode How commands are ordered.
     */
    public DrawList(LayerMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Layer mode cannot be null.");
        }
        this.mode = mode;
        allocate(INITIAL_CAPACITY);
    }

    public LayerMode getMode() {
        return mode;
    }

    /**
     * @return The number of recorded commands.
     */
    public int size() {
        return size;
    }

    /**
     * Remove all commands (capacity is kept).
     */
    public void clear() {
        // drop texture references so the list never keeps disposed textures reachable
        Arrays.fill(textures, 0, size, null);
        size = 0;
        needsSort = false;
        prepared = false;
    }

    /**
     * Make sure at least {@code capacity} commands fit without growing.
     *
     * @param capacity The number of commands.
     */
    public void ensureCapacity(int capacity) {
        if (capacity > textures.length) {
            grow(capacity);
        }
    }

    /**
     * Record a textured quad.
     *
     * @param texture The texture (not null).
     * @param x0      Top-left x.
     * @param y0      Top-left y.
     * @param x1      Top-right x.
     * @param y1      Top-right y.
     * @param x2      Bottom-right x.
     * @param y2      Bottom-right y.
     * @param x3      Bottom-left x.
     * @param y3      Bottom-left y.
     * @param u0      Texture coordinate left.
     * @param v0      Texture coordinate top.
     * @param u1      Texture coordinate right.
     * @param v1      Texture coordinate bottom.
     * @param rgba    Packed color (see {@link Colors#pack}).
     * @param depth   The depth (only used by {@link LayerMode#SORTED}).
     */
    public void addQuad(Texture texture,
                        float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3,
                        float u0, float v0, float u1, float v1, int rgba, float depth) {
        if (texture == null) {
            throw new IllegalArgumentException("Texture cannot be null.");
        }
        if (size == textures.length) {
            grow(size * 2);
        }
        int i = size;
        textures[i] = texture;

        int p = i * 8;
        positions[p] = x0;
        positions[p + 1] = y0;
        positions[p + 2] = x1;
        positions[p + 3] = y1;
        positions[p + 4] = x2;
        positions[p + 5] = y2;
        positions[p + 6] = x3;
        positions[p + 7] = y3;

        int t = i * 4;
        texCoords[t] = u0;
        texCoords[t + 1] = v0;
        texCoords[t + 2] = u1;
        texCoords[t + 3] = v1;

        colors[i] = rgba;

        if (mode == LayerMode.SORTED) {
            keys[i] = SortKeys.depthThenSequence(depth, i);
            if (i == 0) {
                firstDepth = depth;
            } else if (depth != firstDepth) {
                needsSort = true;
            }
        }

        size++;
        prepared = false;
    }

    /**
     * Establish the final draw order. Must be called after recording and before {@link #orderedIndex(int)}.
     */
    public void prepare() {
        if (mode == LayerMode.SORTED && needsSort) {
            sortByDepth();
        } else if (mode == LayerMode.SORTED) {
            // all depths equal: submission order is already the final order
            for (int i = 0; i < size; i++) {
                keys[i] = i;
            }
        }
        needsSort = false;
        prepared = true;
    }

    /**
     * @param order Position in the final draw order (0..size-1).
     * @return The index of the command drawn at that position.
     */
    public int orderedIndex(int order) {
        if (!prepared) {
            throw new IllegalStateException("prepare() must be called before reading the draw order.");
        }
        return mode == LayerMode.SORTED ? SortKeys.sequence(keys[order]) : order;
    }

    public Texture texture(int index) {
        return textures[index];
    }

    /**
     * @param index  The command index.
     * @param corner 0 = top-left, 1 = top-right, 2 = bottom-right, 3 = bottom-left.
     * @return The x coordinate of that corner.
     */
    public float x(int index, int corner) {
        return positions[index * 8 + corner * 2];
    }

    /**
     * @param index  The command index.
     * @param corner 0 = top-left, 1 = top-right, 2 = bottom-right, 3 = bottom-left.
     * @return The y coordinate of that corner.
     */
    public float y(int index, int corner) {
        return positions[index * 8 + corner * 2 + 1];
    }

    public float u0(int index) {
        return texCoords[index * 4];
    }

    public float v0(int index) {
        return texCoords[index * 4 + 1];
    }

    public float u1(int index) {
        return texCoords[index * 4 + 2];
    }

    public float v1(int index) {
        return texCoords[index * 4 + 3];
    }

    public int color(int index) {
        return colors[index];
    }

    /**
     * Stable LSD radix sort of {@code keys[0..size)} by their high 32 bits (the ordered depth). Keys were written in
     * submission order, so stability keeps equal depths in submission order. O(n), allocation-free (unlike
     * {@code Arrays.sort}, which may allocate a merge buffer for partially sorted input).
     */
    private void sortByDepth() {
        long[] src = keys;
        long[] dst = sortBuffer;
        for (int shift = 32; shift < 64; shift += 8) {
            int[] counts = radixCounts;
            Arrays.fill(counts, 0);
            for (int i = 0; i < size; i++) {
                counts[radixByte(src[i], shift)]++;
            }
            if (counts[radixByte(src[0], shift)] == size) {
                continue; // every key has the same byte here: this pass would not change the order
            }
            for (int b = 0, sum = 0; b < 256; b++) {
                int c = counts[b];
                counts[b] = sum;
                sum += c;
            }
            for (int i = 0; i < size; i++) {
                long key = src[i];
                dst[counts[radixByte(key, shift)]++] = key;
            }
            long[] swap = src;
            src = dst;
            dst = swap;
        }
        if (src != keys) {
            System.arraycopy(src, 0, keys, 0, size);
        }
    }

    private static int radixByte(long key, int shift) {
        // flip the sign bit of the depth so negative depths sort before positive ones as unsigned bytes
        return (int) (((key ^ 0x8000000000000000L) >>> shift) & 0xFF);
    }

    private void allocate(int capacity) {
        textures = new Texture[capacity];
        positions = new float[capacity * 8];
        texCoords = new float[capacity * 4];
        colors = new int[capacity];
        keys = new long[capacity];
        sortBuffer = new long[capacity];
    }

    private void grow(int capacity) {
        textures = Arrays.copyOf(textures, capacity);
        positions = Arrays.copyOf(positions, capacity * 8);
        texCoords = Arrays.copyOf(texCoords, capacity * 4);
        colors = Arrays.copyOf(colors, capacity);
        keys = Arrays.copyOf(keys, capacity);
        sortBuffer = new long[capacity];
    }
}
