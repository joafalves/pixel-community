package org.pixel.graphics.draw;

import org.pixel.content.Texture;
import org.pixel.graphics.backend.BackendCapabilities;
import org.pixel.graphics.backend.BatchList;
import org.pixel.graphics.backend.VertexStream;

/**
 * Turns a prepared {@link DrawList} into vertices and batches. Walks the commands in final draw order and merges
 * consecutive commands into one batch until either the batch's texture slots are full or it reaches the backend's
 * maximum quad count. Never reorders; ordering is the draw list's job.
 */
public final class Batcher {

    private Batcher() {
    }

    /**
     * Build vertices and batches for a draw list.
     *
     * @param list         The prepared draw list.
     * @param capabilities The backend limits.
     * @param vertices     The output vertex stream (cleared first).
     * @param batches      The output batches (cleared first).
     */
    public static void build(DrawList list, BackendCapabilities capabilities, VertexStream vertices,
                             BatchList batches) {
        if (batches.getMaxTextureSlots() > capabilities.maxTextureSlots()) {
            throw new IllegalArgumentException("Batch list allows more texture slots than the backend supports.");
        }
        vertices.clear();
        batches.clear();

        int count = list.size();
        if (count == 0) {
            return;
        }
        vertices.reserve(count * 4);

        int maxQuads = capabilities.maxQuadsPerBatch();
        batches.begin(0);
        for (int order = 0; order < count; order++) {
            int index = list.orderedIndex(order);
            Texture texture = list.texture(index);

            if (batches.currentQuadCount() == maxQuads) {
                batches.begin(order);
            }
            int slot = batches.slotOf(texture);
            if (slot < 0) { // texture slots of this batch are full
                batches.begin(order);
                slot = batches.slotOf(texture);
            }

            int rgba = list.color(index);
            float u0 = list.u0(index), v0 = list.v0(index), u1 = list.u1(index), v1 = list.v1(index);
            vertices.putSpriteVertex(list.x(index, 0), list.y(index, 0), u0, v0, rgba, slot);
            vertices.putSpriteVertex(list.x(index, 1), list.y(index, 1), u1, v0, rgba, slot);
            vertices.putSpriteVertex(list.x(index, 2), list.y(index, 2), u1, v1, rgba, slot);
            vertices.putSpriteVertex(list.x(index, 3), list.y(index, 3), u0, v1, rgba, slot);
            batches.addQuad();
        }
    }
}
