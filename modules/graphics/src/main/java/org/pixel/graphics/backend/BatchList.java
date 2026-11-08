package org.pixel.graphics.backend;

import org.pixel.content.Texture;
import org.pixel.graphics.draw.BatchBreak;
import org.pixel.graphics.material.Material;

import java.util.Arrays;

/**
 * The batches of one layer as drawn by a pass (rebuilt by the batcher for each). A batch is a consecutive quad range of the vertex stream of its {@link VertexLayout}
 * that is drawn with one draw call, plus:
 * <ul>
 *   <li>its {@link Material} ({@code null} = the built-in program);</li>
 *   <li>the textures it binds (slot i = texture unit i; a material batch has fewer slots, see
 *   {@link BackendCapabilities#materialTextureSlots()});</li>
 *   <li>for {@link VertexLayout#FULL} batches, its slice of the pass-wide <b>clip table</b> (1 vec4 per clip:
 *   x0, y0, x1, y1) and <b>style table</b> (8 vec4 per style). Each slice starts at an offset aligned to the
 *   backend's uniform buffer alignment, so a backend can bind it directly as a uniform range.</li>
 * </ul>
 * Grows, never shrinks.
 */
public final class BatchList {

    /**
     * Floats per clip table entry (one vec4).
     */
    public static final int CLIP_FLOATS = 4;
    /**
     * Floats per style table entry (8 vec4, see {@code org.pixel.graphics.draw.StyleTable}).
     */
    public static final int STYLE_FLOATS = 32;

    private final int maxTextureSlots;

    private int size;
    private VertexLayout[] layout = new VertexLayout[16];
    private Material[] materials = new Material[16];
    private BatchBreak[] reasons = new BatchBreak[16];
    private int[] slotLimit = new int[16];
    private int[] firstQuad = new int[16];
    private int[] quadCount = new int[16];
    private int[] textureCount = new int[16]; // highest used slot + 1 (slots can have holes, see slotOf)
    private int[] slotsUsed = new int[16];
    private Texture lastTexture; // slotOf cache: consecutive commands usually share a texture
    private int lastSlot;
    private int[] clipOffset = new int[16];
    private int[] clipCount = new int[16];
    private int[] styleOffset = new int[16];
    private int[] styleCount = new int[16];
    private Texture[] textures;

    private float[] clipData = new float[256];
    private int clipDataSize;
    private float[] styleData = new float[512];
    private int styleDataSize;

    /**
     * Constructor.
     *
     * @param maxTextureSlots The maximum number of textures one batch may bind.
     */
    public BatchList(int maxTextureSlots) {
        if (maxTextureSlots < 1) {
            throw new IllegalArgumentException("maxTextureSlots must be at least 1.");
        }
        this.maxTextureSlots = maxTextureSlots;
        this.textures = new Texture[16 * maxTextureSlots];
    }

    public int getMaxTextureSlots() {
        return maxTextureSlots;
    }

    /**
     * Remove all batches and table data (capacity is kept).
     */
    public void clear() {
        lastTexture = null;
        Arrays.fill(textures, 0, size * maxTextureSlots, null);
        Arrays.fill(materials, 0, size, null);
        size = 0;
        clipDataSize = 0;
        styleDataSize = 0;
    }

    /**
     * @return The number of batches (open batch included).
     */
    public int size() {
        return size;
    }

    /**
     * Start a new batch with the built-in program and every texture slot (recorded as {@link BatchBreak#FIRST}).
     *
     * @param batchLayout    The vertex layout of the batch.
     * @param first          The first quad of the batch in the stream of that layout.
     * @param alignmentFloats Alignment of the batch's table slices (in floats).
     */
    public void begin(VertexLayout batchLayout, int first, int alignmentFloats) {
        begin(batchLayout, null, first, alignmentFloats, maxTextureSlots, BatchBreak.FIRST);
    }

    /**
     * Start a new batch.
     *
     * @param batchLayout     The vertex layout of the batch.
     * @param material        The batch's material ({@code null} = built-in program).
     * @param first           The first quad of the batch in the stream of that layout.
     * @param alignmentFloats Alignment of the batch's table slices (in floats).
     * @param slots           Texture slots the batch may use (1 to {@link #getMaxTextureSlots()}).
     * @param reason          Why the previous batch ended ({@link BatchBreak#FIRST} for the first one).
     */
    public void begin(VertexLayout batchLayout, Material material, int first, int alignmentFloats, int slots,
                      BatchBreak reason) {
        if (slots < 1 || slots > maxTextureSlots) {
            throw new IllegalArgumentException("Slots must be 1 to " + maxTextureSlots + ".");
        }
        if (size == firstQuad.length) {
            int capacity = size * 2;
            layout = Arrays.copyOf(layout, capacity);
            materials = Arrays.copyOf(materials, capacity);
            reasons = Arrays.copyOf(reasons, capacity);
            slotLimit = Arrays.copyOf(slotLimit, capacity);
            firstQuad = Arrays.copyOf(firstQuad, capacity);
            quadCount = Arrays.copyOf(quadCount, capacity);
            textureCount = Arrays.copyOf(textureCount, capacity);
            slotsUsed = Arrays.copyOf(slotsUsed, capacity);
            clipOffset = Arrays.copyOf(clipOffset, capacity);
            clipCount = Arrays.copyOf(clipCount, capacity);
            styleOffset = Arrays.copyOf(styleOffset, capacity);
            styleCount = Arrays.copyOf(styleCount, capacity);
            textures = Arrays.copyOf(textures, capacity * maxTextureSlots);
        }
        layout[size] = batchLayout;
        materials[size] = material;
        reasons[size] = reason;
        slotLimit[size] = slots;
        firstQuad[size] = first;
        quadCount[size] = 0;
        textureCount[size] = 0;
        slotsUsed[size] = 0;
        lastTexture = null;
        clipDataSize = align(clipDataSize, alignmentFloats);
        styleDataSize = align(styleDataSize, alignmentFloats);
        clipOffset[size] = clipDataSize;
        clipCount[size] = 0;
        styleOffset[size] = styleDataSize;
        styleCount[size] = 0;
        size++;
    }

    /**
     * Find or assign the slot of a texture in the current (last) batch. A texture the previous batch also used keeps
     * its slot when it is free, so the backend doesn't bind it again; slots can therefore have holes
     * ({@link #texture} returns {@code null} there).
     *
     * @param texture The texture.
     * @return The slot, or -1 if the batch has no free slot left.
     */
    public int slotOf(Texture texture) {
        if (texture == lastTexture) {
            return lastSlot;
        }
        int batch = size - 1;
        int base = batch * maxTextureSlots;
        int count = textureCount[batch];
        for (int s = 0; s < count; s++) {
            if (textures[base + s] == texture) {
                return remember(texture, s);
            }
        }
        int limit = slotLimit[batch];
        if (slotsUsed[batch] == limit) {
            return -1;
        }
        int slot = -1;
        if (batch > 0) { // sticky: the slot it had in the previous batch, if free here
            int previousBase = (batch - 1) * maxTextureSlots;
            for (int s = 0, n = Math.min(textureCount[batch - 1], limit); s < n; s++) {
                if (textures[previousBase + s] == texture) {
                    if (s >= count || textures[base + s] == null) {
                        slot = s;
                    }
                    break;
                }
            }
        }
        if (slot < 0) { // the first free slot
            slot = 0;
            while (slot < count && textures[base + slot] != null) {
                slot++;
            }
        }
        textures[base + slot] = texture;
        slotsUsed[batch]++;
        textureCount[batch] = Math.max(count, slot + 1);
        return remember(texture, slot);
    }

    private int remember(Texture texture, int slot) {
        lastTexture = texture;
        lastSlot = slot;
        return slot;
    }

    /**
     * Append a clip rectangle to the current (last) batch's clip table.
     *
     * @return The batch-local, 1-based clip index.
     */
    public int addClip(float x0, float y0, float x1, float y1) {
        ensureClipData(clipDataSize + CLIP_FLOATS);
        int i = clipDataSize;
        clipData[i] = x0;
        clipData[i + 1] = y0;
        clipData[i + 2] = x1;
        clipData[i + 3] = y1;
        clipDataSize += CLIP_FLOATS;
        return ++clipCount[size - 1];
    }

    /**
     * Reserve a style entry in the current (last) batch's style table; the caller writes {@link #STYLE_FLOATS}
     * floats into {@link #styleData()} at the returned offset.
     *
     * @return The float offset of the new entry in {@link #styleData()}.
     */
    public int reserveStyle() {
        ensureStyleData(styleDataSize + STYLE_FLOATS);
        int offset = styleDataSize;
        styleDataSize += STYLE_FLOATS;
        styleCount[size - 1]++;
        return offset;
    }

    /**
     * Add one quad to the current (last) batch.
     */
    public void addQuad() {
        quadCount[size - 1]++;
    }

    /**
     * @return The quad count of the current (last) batch.
     */
    public int currentQuadCount() {
        return quadCount[size - 1];
    }

    /**
     * @return The layout of the current (last) batch, or {@code null} if there is none.
     */
    public VertexLayout currentLayout() {
        return size == 0 ? null : layout[size - 1];
    }

    public int currentClipCount() {
        return clipCount[size - 1];
    }

    public int currentStyleCount() {
        return styleCount[size - 1];
    }

    /**
     * @return The material of the current (last) batch ({@code null} = built-in, also when there is no batch).
     */
    public Material currentMaterial() {
        return size == 0 ? null : materials[size - 1];
    }

    /**
     * @return Whether the current (last) batch has no free texture slot left.
     */
    public boolean currentSlotsFull() {
        return slotsUsed[size - 1] == slotLimit[size - 1];
    }

    public VertexLayout layout(int batch) {
        return layout[batch];
    }

    /**
     * @return The batch's material, or {@code null} for the built-in program.
     */
    public Material material(int batch) {
        return materials[batch];
    }

    /**
     * @return Why the batch before it ended ({@link BatchBreak#FIRST} for the first batch).
     */
    public BatchBreak reason(int batch) {
        return reasons[batch];
    }

    /**
     * @return The texture slots the batch may use.
     */
    public int slotLimit(int batch) {
        return slotLimit[batch];
    }

    public int firstQuad(int batch) {
        return firstQuad[batch];
    }

    public int quadCount(int batch) {
        return quadCount[batch];
    }

    /**
     * @return The slots the batch spans: its highest used slot + 1 (some below may be empty, see {@link #slotOf}).
     */
    public int textureCount(int batch) {
        return textureCount[batch];
    }

    /**
     * @param batch The batch.
     * @param slot  The slot (0..textureCount-1).
     * @return The texture bound to that slot.
     */
    public Texture texture(int batch, int slot) {
        return textures[batch * maxTextureSlots + slot];
    }

    /**
     * @return Float offset of the batch's clip slice in {@link #clipData()}.
     */
    public int clipOffset(int batch) {
        return clipOffset[batch];
    }

    public int clipCount(int batch) {
        return clipCount[batch];
    }

    /**
     * @return Float offset of the batch's style slice in {@link #styleData()}.
     */
    public int styleOffset(int batch) {
        return styleOffset[batch];
    }

    public int styleCount(int batch) {
        return styleCount[batch];
    }

    /**
     * @return The pass-wide clip table (valid floats: {@link #clipDataSize()}). Shared, not a copy.
     */
    public float[] clipData() {
        return clipData;
    }

    public int clipDataSize() {
        return clipDataSize;
    }

    /**
     * @return The pass-wide style table (valid floats: {@link #styleDataSize()}). Shared, not a copy.
     */
    public float[] styleData() {
        return styleData;
    }

    public int styleDataSize() {
        return styleDataSize;
    }

    private static int align(int value, int alignment) {
        return (value + alignment - 1) / alignment * alignment;
    }

    private void ensureClipData(int needed) {
        if (needed > clipData.length) {
            clipData = Arrays.copyOf(clipData, Math.max(needed, clipData.length * 2));
        }
    }

    private void ensureStyleData(int needed) {
        if (needed > styleData.length) {
            styleData = Arrays.copyOf(styleData, Math.max(needed, styleData.length * 2));
        }
    }
}
