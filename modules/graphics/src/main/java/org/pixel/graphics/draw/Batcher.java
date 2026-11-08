package org.pixel.graphics.draw;

import org.pixel.content.Texture;
import org.pixel.graphics.backend.BackendCapabilities;
import org.pixel.graphics.backend.BatchList;
import org.pixel.graphics.backend.RenderData;
import org.pixel.graphics.backend.VertexLayout;
import org.pixel.graphics.backend.VertexStream;
import org.pixel.graphics.material.Material;

import java.util.Arrays;

/**
 * Turns a prepared {@link DrawList} into vertices and batches. Walks the commands in final draw order and merges
 * consecutive commands into one batch until one of these breaks it:
 * <ul>
 *   <li>the vertex layout changes (sprite quads vs shape/canvas quads);</li>
 *   <li>the material changes;</li>
 *   <li>the batch reaches the backend's maximum quad count;</li>
 *   <li>a new texture doesn't fit in the batch's texture slots;</li>
 *   <li>a new clip rectangle or style doesn't fit in the batch's clip/style table.</li>
 * </ul>
 * Never reorders; ordering is the draw list's job. Reuses its scratch arrays, so building is allocation-free once
 * warmed up. Not thread-safe.
 */
public final class Batcher {

    private int batchSerial;
    // frame clip/style id -> batch-local index, valid when the stamp equals the current batch serial
    private int[] clipLocal = new int[64];
    private int[] clipStamp = new int[64];
    private int[] styleLocal = new int[64];
    private int[] styleStamp = new int[64];

    /**
     * Build vertices and batches for a draw list.
     *
     * @param list         The prepared draw list.
     * @param capabilities The backend limits.
     * @param out          The output (cleared first).
     */
    public void build(DrawList list, BackendCapabilities capabilities, RenderData out) {
        build(list, capabilities, out, null);
    }

    /**
     * Build vertices and batches for a draw list, skipping commands entirely outside the given bounds.
     *
     * @param list         The prepared draw list.
     * @param capabilities The backend limits.
     * @param out          The output (cleared first).
     * @param cullBounds   Visible area in layer coordinates ({@code x0, y0, x1, y1}), or {@code null} to keep all.
     * @return The number of commands culled.
     */
    public int build(DrawList list, BackendCapabilities capabilities, RenderData out, float[] cullBounds) {
        BatchList batches = out.batches();
        if (batches.getMaxTextureSlots() > capabilities.maxTextureSlots()) {
            throw new IllegalArgumentException("Batch list allows more texture slots than the backend supports.");
        }
        out.clear();
        int count = list.size();
        if (count == 0) {
            return 0;
        }
        int culled = 0;
        ensureScratch(list.clips().size() + 1, list.styles().size() + 1);

        int maxQuads = capabilities.maxQuadsPerBatch();
        int alignment = capabilities.tableAlignmentFloats();
        for (int order = 0; order < count; order++) {
            int index = list.orderedIndex(order);
            if (cullBounds != null && outside(list, index, cullBounds)) {
                culled++;
                continue;
            }
            VertexLayout layout = list.layout(index);
            VertexStream vertices = out.vertices(layout);
            Material material = list.material(index);

            if (batches.currentLayout() != layout || batches.currentMaterial() != material
                    || batches.currentQuadCount() == maxQuads || !fits(list, index, batches, capabilities)) {
                beginBatch(batches, layout, material, vertices.quadCount(), alignment, capabilities);
            }

            Texture texture = list.texture(index);
            int slot = texture != null ? batches.slotOf(texture) : 0;
            vertices.reserve(4);
            if (layout == VertexLayout.SPRITE) {
                writeSprite(list, index, slot, vertices);
            } else {
                int clip = resolveClip(list, index, batches);
                int style = resolveStyle(list, index, batches);
                writeFull(list, index, slot, clip, style, vertices);
            }
            batches.addQuad();
        }
        return culled;
    }

    /** Whether the command's quad (anti-aliasing margin, strokes and shadows included) misses the bounds. */
    private static boolean outside(DrawList list, int index, float[] bounds) {
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        for (int corner = 0; corner < 4; corner++) {
            float x = list.x(index, corner), y = list.y(index, corner);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        return maxX < bounds[0] || minX > bounds[2] || maxY < bounds[1] || minY > bounds[3];
    }

    private void beginBatch(BatchList batches, VertexLayout layout, Material material, int firstQuad, int alignment,
                            BackendCapabilities capabilities) {
        int slots = material != null ? Math.min(capabilities.materialTextureSlots(), batches.getMaxTextureSlots())
                : batches.getMaxTextureSlots();
        batches.begin(layout, material, firstQuad, alignment, slots);
        batchSerial++;
    }

    /**
     * Whether the command's texture, clip and style fit into the current batch (without changing it).
     */
    private boolean fits(DrawList list, int index, BatchList batches, BackendCapabilities capabilities) {
        Texture texture = list.texture(index);
        if (texture != null && batches.currentSlotsFull() && !hasTexture(batches, texture)) {
            return false;
        }
        if (list.layout(index) == VertexLayout.FULL) {
            int clip = list.clipId(index);
            if (clip != 0 && clipStamp[clip] != batchSerial
                    && batches.currentClipCount() == capabilities.maxClipsPerBatch()) {
                return false;
            }
            int style = list.styleId(index);
            return style == 0 || styleStamp[style] == batchSerial
                    || batches.currentStyleCount() < capabilities.maxStylesPerBatch();
        }
        return true;
    }

    private static boolean hasTexture(BatchList batches, Texture texture) {
        int batch = batches.size() - 1;
        for (int s = 0, n = batches.textureCount(batch); s < n; s++) {
            if (batches.texture(batch, s) == texture) {
                return true;
            }
        }
        return false;
    }

    private int resolveClip(DrawList list, int index, BatchList batches) {
        int id = list.clipId(index);
        if (id == 0) {
            return 0;
        }
        if (clipStamp[id] != batchSerial) {
            ClipTable clips = list.clips();
            clipLocal[id] = batches.addClip(clips.x0(id), clips.y0(id), clips.x1(id), clips.y1(id));
            clipStamp[id] = batchSerial;
        }
        return clipLocal[id];
    }

    private int resolveStyle(DrawList list, int index, BatchList batches) {
        int id = list.styleId(index);
        if (id == 0) {
            return 0;
        }
        if (styleStamp[id] != batchSerial) {
            int offset = batches.reserveStyle();
            list.styles().copy(id, batches.styleData(), offset);
            styleLocal[id] = batches.currentStyleCount();
            styleStamp[id] = batchSerial;
        }
        return styleLocal[id];
    }

    private static void writeSprite(DrawList list, int index, int slot, VertexStream vertices) {
        int rgba = list.color(index);
        float u0 = list.u0(index), v0 = list.v0(index), u1 = list.u1(index), v1 = list.v1(index);
        vertices.putSpriteVertex(list.x(index, 0), list.y(index, 0), u0, v0, rgba, slot);
        vertices.putSpriteVertex(list.x(index, 1), list.y(index, 1), u1, v0, rgba, slot);
        vertices.putSpriteVertex(list.x(index, 2), list.y(index, 2), u1, v1, rgba, slot);
        vertices.putSpriteVertex(list.x(index, 3), list.y(index, 3), u0, v1, rgba, slot);
    }

    private static void writeFull(DrawList list, int index, int slot, int clip, int style, VertexStream vertices) {
        int rgba = list.color(index);
        float p0 = list.param(index, 0), p1 = list.param(index, 1), p2 = list.param(index, 2);
        float p3 = list.param(index, 3);
        int type = list.shapeType(index);
        for (int corner = 0; corner < 4; corner++) {
            vertices.putFullVertex(list.x(index, corner), list.y(index, corner),
                    list.cornerU(index, corner), list.cornerV(index, corner), rgba,
                    list.localX(index, corner), list.localY(index, corner), p0, p1, p2, p3, type, slot, clip, style);
        }
    }

    private void ensureScratch(int clips, int styles) {
        if (clips > clipLocal.length) {
            int capacity = Math.max(clips, clipLocal.length * 2);
            clipLocal = Arrays.copyOf(clipLocal, capacity);
            clipStamp = Arrays.copyOf(clipStamp, capacity);
        }
        if (styles > styleLocal.length) {
            int capacity = Math.max(styles, styleLocal.length * 2);
            styleLocal = Arrays.copyOf(styleLocal, capacity);
            styleStamp = Arrays.copyOf(styleStamp, capacity);
        }
        // stamps from previous frames must not match: ids are reused every frame
        if (batchSerial > Integer.MAX_VALUE - 1_000_000) {
            Arrays.fill(clipStamp, 0);
            Arrays.fill(styleStamp, 0);
            batchSerial = 0;
        }
    }
}
