package org.pixel.graphics.draw;

import org.pixel.content.Texture;
import org.pixel.graphics.backend.VertexLayout;
import org.pixel.graphics.material.Material;

import java.util.Arrays;

/**
 * GL-free, allocation-free storage of the draw commands of one layer for one frame.
 * <p>
 * Every command is a quad: either a textured sprite quad ({@link VertexLayout#SPRITE}, see
 * {@link #addQuad}) or a shape/canvas quad ({@link VertexLayout#FULL}, see {@link #addShape}) that also carries a
 * {@link ShapeType}, per-corner texture/shape coordinates and local coordinates, shape parameters and ids into this
 * list's {@link StyleTable} and
 * {@link ClipTable}. Commands are stored struct-of-arrays in primitive arrays that grow but never shrink. After
 * {@link #prepare()}, {@link #orderedIndex(int)} gives the commands in final draw order (see {@link LayerMode}).
 * Front-ends (SpriteBatch, Canvas, ...) record here; a {@link Batcher} turns the result into GPU batches.
 * <p>
 * Shape commands carry the {@link Material} set with {@link #setMaterial} when they were added; material data is
 * merged into the command's style record ({@link StyleTable#MATERIAL_DATA}).
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
    private float[] cornerUv;    // 8 per quad: texture coordinates or shape dimensions per corner (FULL only)
    private float[] cornerLocal; // 8 per quad: shape-local coordinates per corner (FULL only)
    private float[] params;    // 4 per quad (FULL only)
    private int[] styleIds;    // StyleTable id, 0 = none (FULL only)
    private int[] clipIds;     // ClipTable id, 0 = none (FULL only)
    private Material[] materials; // null = built-in program (FULL only)
    private final StyleTable styles = new StyleTable();
    private final ClipTable clips = new ClipTable();
    private long[] keys;       // sorted modes: depth (high 32 bits), then material and texture group (low 32 bits)
    private int[] sequence;    // sorted modes: the command index travelling with its key
    private long[] keyBuffer;  // radix sort scratch
    private int[] sequenceBuffer;
    private final int[] radixCounts = new int[256];
    private boolean needsSort;
    private long firstKey;
    private boolean prepared;

    // material applied to shape commands being added, and its per-command data
    private Material material;
    private boolean hasMaterialData;
    private final float[] materialData = new float[4];
    private final float[] styleScratch = new float[StyleTable.FLOATS];
    private int dataBaseStyle = -1;   // cache: style id without data ...
    private int dataMergedStyle;      // ... and with the current data merged in

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
        // drop texture and material references so the list never keeps disposed ones reachable
        Arrays.fill(textures, 0, size, null);
        Arrays.fill(materials, 0, size, null);
        size = 0;
        setMaterial(null);
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
        if (material != null) {
            throw new IllegalStateException("Materials apply to shape commands, not sprite quads.");
        }
        int i = append(texture, null, x0, y0, x1, y1, x2, y2, x3, y3, u0, v0, u1, v1, rgba, depth);
        layouts[i] = VertexLayout.SPRITE;
    }

    /**
     * Record a shape or canvas quad ({@link VertexLayout#FULL}).
     *
     * @param type      The shape type.
     * @param texture   The texture (types where {@link ShapeType#isTextured()}, otherwise {@code null}).
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
        if (type.isTextured() != (texture != null)) {
            throw new IllegalArgumentException("A texture is required for (and only for) textured shapes.");
        }
        int i = append(texture, material, x0, y0, x1, y1, x2, y2, x3, y3, u0, v0, u1, v1, rgba, depth);
        layouts[i] = VertexLayout.FULL;
        shapeTypes[i] = (byte) type.code();
        int c = i * 8; // corners: top-left, top-right, bottom-right, bottom-left
        cornerUv[c] = u0;
        cornerUv[c + 1] = v0;
        cornerUv[c + 2] = u1;
        cornerUv[c + 3] = v0;
        cornerUv[c + 4] = u1;
        cornerUv[c + 5] = v1;
        cornerUv[c + 6] = u0;
        cornerUv[c + 7] = v1;
        cornerLocal[c] = -extentX;
        cornerLocal[c + 1] = -extentY;
        cornerLocal[c + 2] = extentX;
        cornerLocal[c + 3] = -extentY;
        cornerLocal[c + 4] = extentX;
        cornerLocal[c + 5] = extentY;
        cornerLocal[c + 6] = -extentX;
        cornerLocal[c + 7] = extentY;
        int p = i * 4;
        params[p] = p0;
        params[p + 1] = p1;
        params[p + 2] = p2;
        params[p + 3] = p3;
        styleIds[i] = withMaterialData(styleId);
        clipIds[i] = clipId;
    }

    /**
     * Record a {@link VertexLayout#FULL} quad with arbitrary per-corner data (path geometry). Triangles are quads
     * whose last corner repeats the first one.
     *
     * @param type      The shape type.
     * @param positions 8 floats: x, y of the 4 corners (layer space), in quad order.
     * @param uv        8 floats: per-corner texture coordinates / shape data.
     * @param local     8 floats: per-corner local coordinates.
     * @param p0        Shape parameter 0.
     * @param p1        Shape parameter 1.
     * @param p2        Shape parameter 2.
     * @param p3        Shape parameter 3.
     * @param rgba      Packed fill color.
     * @param styleId   Id in {@link #styles()} (0 = no style).
     * @param clipId    Id in {@link #clips()} (0 = no clip).
     * @param depth     The depth (only used by {@link LayerMode#SORTED}).
     */
    public void addShapeCorners(ShapeType type, float[] positions, float[] uv, float[] local,
                                float p0, float p1, float p2, float p3, int rgba, int styleId, int clipId,
                                float depth) {
        addShapeCorners(type, null, positions, uv, local, p0, p1, p2, p3, rgba, styleId, clipId, depth);
    }

    /**
     * Record a {@link VertexLayout#FULL} quad with arbitrary per-corner data, sampling {@code texture} (glyphs).
     *
     * @see #addShapeCorners(ShapeType, float[], float[], float[], float, float, float, float, int, int, int, float)
     */
    public void addShapeCorners(ShapeType type, Texture texture, float[] positions, float[] uv, float[] local,
                                float p0, float p1, float p2, float p3, int rgba, int styleId, int clipId,
                                float depth) {
        if (type.isTextured() != (texture != null)) {
            throw new IllegalArgumentException("A texture is required for (and only for) textured shapes.");
        }
        int i = append(texture, material, positions[0], positions[1], positions[2], positions[3], positions[4],
                positions[5], positions[6], positions[7], 0f, 0f, 0f, 0f, rgba, depth);
        layouts[i] = VertexLayout.FULL;
        shapeTypes[i] = (byte) type.code();
        System.arraycopy(uv, 0, cornerUv, i * 8, 8);
        System.arraycopy(local, 0, cornerLocal, i * 8, 8);
        int p = i * 4;
        params[p] = p0;
        params[p + 1] = p1;
        params[p + 2] = p2;
        params[p + 3] = p3;
        styleIds[i] = withMaterialData(styleId);
        clipIds[i] = clipId;
    }

    /**
     * Set the material of the shape commands added from now on ({@code null} = the built-in program), without
     * material data.
     */
    public void setMaterial(Material material) {
        this.material = material;
        this.hasMaterialData = false;
        dataBaseStyle = -1;
    }

    /**
     * Set the material of the shape commands added from now on, with per-command data ({@code Fragment.data}).
     */
    public void setMaterial(Material material, float x, float y, float z, float w) {
        if (material == null) {
            throw new IllegalArgumentException("Material data needs a material.");
        }
        this.material = material;
        this.hasMaterialData = true;
        materialData[0] = x;
        materialData[1] = y;
        materialData[2] = z;
        materialData[3] = w;
        dataBaseStyle = -1;
    }

    /** The style id with the current material data merged in (cached: a path or text adds many quads). */
    private int withMaterialData(int styleId) {
        if (!hasMaterialData) {
            return styleId;
        }
        if (styleId != dataBaseStyle) {
            if (styleId == 0) {
                Arrays.fill(styleScratch, 0f); // no stroke, solid paint, no shadow: only the data
            } else {
                styles.copy(styleId, styleScratch, 0);
            }
            System.arraycopy(materialData, 0, styleScratch, StyleTable.MATERIAL_DATA, 4);
            dataMergedStyle = styles.intern(styleScratch);
            dataBaseStyle = styleId;
        }
        return dataMergedStyle;
    }

    private int append(Texture texture, Material commandMaterial,
                       float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3,
                       float u0, float v0, float u1, float v1, int rgba, float depth) {
        if (size == textures.length) {
            grow(size * 2);
        }
        int i = size;
        textures[i] = texture;
        materials[i] = commandMaterial;

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

        if (mode != LayerMode.ORDERED) {
            long key = (long) SortKeys.orderedDepth(depth) << 32;
            if (mode == LayerMode.SORTED_BY_MATERIAL) {
                int materialId = commandMaterial != null ? commandMaterial.getId() : 0;
                int textureGroup = texture != null ? System.identityHashCode(texture) & 0xFFFF : 0;
                key |= ((long) materialId << 16) | textureGroup;
            }
            keys[i] = key;
            sequence[i] = i;
            if (i == 0) {
                firstKey = key;
            } else if (key != firstKey) {
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
        if (needsSort) { // otherwise every key is equal: submission order is already the final order
            sortKeys(mode == LayerMode.SORTED_BY_MATERIAL ? 0 : 32);
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
        return mode == LayerMode.ORDERED ? order : sequence[order];
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

    /**
     * @param index  The command index ({@link VertexLayout#FULL}).
     * @param corner 0 = top-left, 1 = top-right, 2 = bottom-right, 3 = bottom-left.
     * @return The u (texture coordinate or shape data) of that corner.
     */
    public float cornerU(int index, int corner) {
        return cornerUv[index * 8 + corner * 2];
    }

    public float cornerV(int index, int corner) {
        return cornerUv[index * 8 + corner * 2 + 1];
    }

    /**
     * @param index  The command index ({@link VertexLayout#FULL}).
     * @param corner 0 = top-left, 1 = top-right, 2 = bottom-right, 3 = bottom-left.
     * @return The local x of that corner.
     */
    public float localX(int index, int corner) {
        return cornerLocal[index * 8 + corner * 2];
    }

    public float localY(int index, int corner) {
        return cornerLocal[index * 8 + corner * 2 + 1];
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
     * @return The command's material, or {@code null} for the built-in program.
     */
    public Material material(int index) {
        return materials[index];
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
     * Stable LSD radix sort of {@code keys[0..size)} (and their command indices) by the key bits from
     * {@code fromShift} up: 32 sorts by depth only, 0 also by material and texture group. Keys were written in
     * submission order, so stability keeps equal keys in submission order. O(n), allocation-free (unlike
     * {@code Arrays.sort}, which may allocate a merge buffer for partially sorted input).
     */
    private void sortKeys(int fromShift) {
        long[] src = keys, dst = keyBuffer;
        int[] srcSeq = sequence, dstSeq = sequenceBuffer;
        for (int shift = fromShift; shift < 64; shift += 8) {
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
                int at = counts[radixByte(key, shift)]++;
                dst[at] = key;
                dstSeq[at] = srcSeq[i];
            }
            long[] swap = src;
            src = dst;
            dst = swap;
            int[] swapSeq = srcSeq;
            srcSeq = dstSeq;
            dstSeq = swapSeq;
        }
        if (src != keys) {
            System.arraycopy(src, 0, keys, 0, size);
            System.arraycopy(srcSeq, 0, sequence, 0, size);
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
        cornerUv = new float[capacity * 8];
        cornerLocal = new float[capacity * 8];
        params = new float[capacity * 4];
        styleIds = new int[capacity];
        clipIds = new int[capacity];
        materials = new Material[capacity];
        keys = new long[capacity];
        sequence = new int[capacity];
        keyBuffer = new long[capacity];
        sequenceBuffer = new int[capacity];
    }

    private void grow(int capacity) {
        textures = Arrays.copyOf(textures, capacity);
        positions = Arrays.copyOf(positions, capacity * 8);
        texCoords = Arrays.copyOf(texCoords, capacity * 4);
        colors = Arrays.copyOf(colors, capacity);
        layouts = Arrays.copyOf(layouts, capacity);
        shapeTypes = Arrays.copyOf(shapeTypes, capacity);
        cornerUv = Arrays.copyOf(cornerUv, capacity * 8);
        cornerLocal = Arrays.copyOf(cornerLocal, capacity * 8);
        params = Arrays.copyOf(params, capacity * 4);
        styleIds = Arrays.copyOf(styleIds, capacity);
        clipIds = Arrays.copyOf(clipIds, capacity);
        materials = Arrays.copyOf(materials, capacity);
        keys = Arrays.copyOf(keys, capacity);
        sequence = Arrays.copyOf(sequence, capacity);
        keyBuffer = new long[capacity];
        sequenceBuffer = new int[capacity];
    }
}
