package org.pixel.graphics.backend;

import org.pixel.content.Texture;

import java.util.Arrays;

/**
 * The batches of one pass: consecutive quad ranges of a {@link VertexStream} that can each be drawn with one draw
 * call, plus the textures each batch binds (slot i of a batch = texture unit i). Grows, never shrinks.
 */
public final class BatchList {

    private final int maxTextureSlots;

    private int size;
    private int[] firstQuad = new int[16];
    private int[] quadCount = new int[16];
    private int[] textureCount = new int[16];
    private Texture[] textures;

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
     * Remove all batches (capacity is kept).
     */
    public void clear() {
        Arrays.fill(textures, 0, size * maxTextureSlots, null);
        size = 0;
    }

    /**
     * @return The number of batches (open batch included).
     */
    public int size() {
        return size;
    }

    /**
     * Start a new batch.
     *
     * @param first The first quad of the batch in the vertex stream.
     */
    public void begin(int first) {
        if (size == firstQuad.length) {
            int capacity = size * 2;
            firstQuad = Arrays.copyOf(firstQuad, capacity);
            quadCount = Arrays.copyOf(quadCount, capacity);
            textureCount = Arrays.copyOf(textureCount, capacity);
            textures = Arrays.copyOf(textures, capacity * maxTextureSlots);
        }
        firstQuad[size] = first;
        quadCount[size] = 0;
        textureCount[size] = 0;
        size++;
    }

    /**
     * Find or assign the slot of a texture in the current (last) batch.
     *
     * @param texture The texture.
     * @return The slot, or -1 if the batch has no free slot left.
     */
    public int slotOf(Texture texture) {
        int batch = size - 1;
        int base = batch * maxTextureSlots;
        int count = textureCount[batch];
        for (int s = 0; s < count; s++) {
            if (textures[base + s] == texture) {
                return s;
            }
        }
        if (count == maxTextureSlots) {
            return -1;
        }
        textures[base + count] = texture;
        textureCount[batch] = count + 1;
        return count;
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

    public int firstQuad(int batch) {
        return firstQuad[batch];
    }

    public int quadCount(int batch) {
        return quadCount[batch];
    }

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
}
