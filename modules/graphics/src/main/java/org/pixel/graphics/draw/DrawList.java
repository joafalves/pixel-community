package org.pixel.graphics.draw;

import org.pixel.content.Texture;
import org.pixel.graphics.backend.VertexLayout;

import java.util.Arrays;

/**
 * GL-free, allocation-free storage of the draw commands of one layer for one frame.
 * <p>
 * Every command is a quad: either a textured sprite quad ({@link VertexLayout#SPRITE}, see
 * {@link #addQuad}) or a shape/canvas quad ({@link VertexLayout#FULL}, see {@link #addShape}) that also carries a
 * {@link ShapeType}, shape-local extents, shape parameters and ids into this list's {@link StyleTable} and
 * {@link ClipTable}. Commands are stored struct-of-arrays in primitive arrays that grow but never shrink. After
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
    private VertexLayout[] layouts;
    private byte[] shapeTypes;
    private float[] extents;   // 2 per quad: shape-local half extents (FULL only)
    private float[] params;    // 4 per quad (FULL only)
    private int[] styleIds;    // StyleTable id, 0 = none (FULL only)
    private int[] clipIds;     // ClipTable id, 0 = none (FULL only)
    private final StyleTable styles = new StyleTable();
    private final ClipTable clips = new ClipTable();
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
        styles.clear();
        clips.clear();
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
        int i = append(texture, x0, y0, x1, y1, x2, y2, x3, y3, u0, v0, u1, v1, rgba, depth);
        layouts[i] = VertexLayout.SPRITE;
    }

    /**
     * Record a shape or canvas quad ({@link VertexLayout#FULL}).
     *
     * @param type      The shape type.
     * @param texture   The texture ({@link ShapeType#TEXTURED} only, otherwise {@code null}).
     * @param x0        Top-left x (layer space, transform already applied).
     * @param y0        Top-left y.
     * @param x1        Top-right x.
     * @param y1        Top-right y.
     * @param x2        Bottom-right x.
     * @param y2        Bottom-right y.
     * @param x3        Bottom-left x.
     * @param y3        Bottom-left y.
     * @param u0        Texture coordinate left, or shape dimension x (with {@code u1 == u0}).
     * @param v0        Texture coordinate top, or shape dimension y (with {@code v1 == v0}).
     * @param u1        Texture coordinate right.
     * @param v1        Texture coordinate bottom.
     * @param extentX   Shape-local half extent x of the quad (local x runs from -extentX to +extentX).
     * @param extentY   Shape-local half extent y of the quad.
     * @param p0        Shape parameter 0.
     * @param p1        Shape parameter 1.
     * @param p2        Shape parameter 2.
     * @param p3        Shape parameter 3.
     * @param rgba      Packed fill color.
     * @param styleId   Id in {@link #styles()} (0 = no stroke).
     * @param clipId    Id in {@link #clips()} (0 = no clip).
     * @param depth     The depth (only used by {@link LayerMode#SORTED}).
     */
    public void addShape(ShapeType type, Texture texture,
                         float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3,
                         float u0, float v0, float u1, float v1, float extentX, float extentY,
                         float p0, float p1, float p2, float p3, int rgba, int styleId, int clipId, float depth) {
        if ((type == ShapeType.TEXTURED) != (texture != null)) {
            throw new IllegalArgumentException("A texture is required for (and only for) textured shapes.");
        }
        int i = append(texture, x0, y0, x1, y1, x2, y2, x3, y3, u0, v0, u1, v1, rgba, depth);
        layouts[i] = VertexLayout.FULL;
        shapeTypes[i] = (byte) type.code();
        extents[i * 2] = extentX;
        extents[i * 2 + 1] = extentY;
        int p = i * 4;
        params[p] = p0;
        params[p + 1] = p1;
        params[p + 2] = p2;
        params[p + 3] = p3;
        styleIds[i] = styleId;
        clipIds[i] = clipId;
    }

    private int append(Texture texture,
                       float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3,
                       float u0, float v0, float u1, float v1, int rgba, float depth) {
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
        return i;
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

    public VertexLayout layout(int index) {
        return layouts[index];
    }

    /**
     * @return The {@link ShapeType#code()} of a {@link VertexLayout#FULL} command.
     */
    public int shapeType(int index) {
        return shapeTypes[index];
    }

    public float extentX(int index) {
        return extents[index * 2];
    }

    public float extentY(int index) {
        return extents[index * 2 + 1];
    }

    /**
     * @param index The command index.
     * @param k     Parameter 0..3.
     * @return The shape parameter.
     */
    public float param(int index, int k) {
        return params[index * 4 + k];
    }

    public int styleId(int index) {
        return styleIds[index];
    }

    public int clipId(int index) {
        return clipIds[index];
    }

    /**
     * @return The frame-local stroke styles referenced by commands of this list.
     */
    public StyleTable styles() {
        return styles;
    }

    /**
     * @return The frame-local clip rectangles referenced by commands of this list.
     */
    public ClipTable clips() {
        return clips;
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
        layouts = new VertexLayout[capacity];
        shapeTypes = new byte[capacity];
        extents = new float[capacity * 2];
        params = new float[capacity * 4];
        styleIds = new int[capacity];
        clipIds = new int[capacity];
        keys = new long[capacity];
        sortBuffer = new long[capacity];
    }

    private void grow(int capacity) {
        textures = Arrays.copyOf(textures, capacity);
        positions = Arrays.copyOf(positions, capacity * 8);
        texCoords = Arrays.copyOf(texCoords, capacity * 4);
        colors = Arrays.copyOf(colors, capacity);
        layouts = Arrays.copyOf(layouts, capacity);
        shapeTypes = Arrays.copyOf(shapeTypes, capacity);
        extents = Arrays.copyOf(extents, capacity * 2);
        params = Arrays.copyOf(params, capacity * 4);
        styleIds = Arrays.copyOf(styleIds, capacity);
        clipIds = Arrays.copyOf(clipIds, capacity);
        keys = Arrays.copyOf(keys, capacity);
        sortBuffer = new long[capacity];
    }
}
