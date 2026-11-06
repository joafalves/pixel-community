package org.pixel.graphics.draw;

import org.junit.jupiter.api.Test;
import org.pixel.content.Texture;
import org.pixel.graphics.backend.BackendCapabilities;
import org.pixel.graphics.backend.BatchList;
import org.pixel.graphics.backend.VertexLayout;
import org.pixel.graphics.backend.VertexStream;
import org.pixel.graphics.testing.TestTexture;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class BatcherTest {

    private static final BackendCapabilities CAPS = new BackendCapabilities(2, 3);

    private final Texture a = new TestTexture(8, 8);
    private final Texture b = new TestTexture(8, 8);
    private final Texture c = new TestTexture(8, 8);

    private static void quad(DrawList list, Texture texture) {
        list.addQuad(texture, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0xFFFFFFFF, 0);
    }

    private static BatchList build(DrawList list, VertexStream vertices) {
        BatchList batches = new BatchList(CAPS.maxTextureSlots());
        list.prepare();
        Batcher.build(list, CAPS, vertices, batches);
        return batches;
    }

    @Test
    void consecutiveQuadsShareABatchUntilTheQuadLimit() {
        DrawList list = new DrawList(LayerMode.ORDERED);
        for (int i = 0; i < 7; i++) quad(list, a);
        VertexStream vertices = new VertexStream(VertexLayout.SPRITE, 4);
        BatchList batches = build(list, vertices);

        assertEquals(3, batches.size()); // 3 + 3 + 1
        assertEquals(0, batches.firstQuad(0));
        assertEquals(3, batches.firstQuad(1));
        assertEquals(6, batches.firstQuad(2));
        assertEquals(1, batches.quadCount(2));
        assertEquals(28, vertices.vertexCount());
    }

    @Test
    void texturesShareABatchWhileSlotsAreFree() {
        DrawList list = new DrawList(LayerMode.ORDERED);
        quad(list, a);
        quad(list, b);
        quad(list, a); // a and b fit in 2 slots
        BatchList batches = build(list, new VertexStream(VertexLayout.SPRITE, 4));

        assertEquals(1, batches.size());
        assertEquals(2, batches.textureCount(0));
        assertSame(a, batches.texture(0, 0));
        assertSame(b, batches.texture(0, 1));
    }

    @Test
    void aThirdTextureStartsANewBatch() {
        DrawList list = new DrawList(LayerMode.ORDERED);
        quad(list, a);
        quad(list, b);
        quad(list, c);
        BatchList batches = build(list, new VertexStream(VertexLayout.SPRITE, 4));

        assertEquals(2, batches.size());
        assertEquals(1, batches.quadCount(1));
        assertSame(c, batches.texture(1, 0));
    }

    @Test
    void verticesCarryPositionsTexCoordsColorAndSlot() {
        DrawList list = new DrawList(LayerMode.ORDERED);
        quad(list, a);
        list.addQuad(b, 10, 20, 30, 20, 30, 40, 10, 40, 0.25f, 0.5f, 0.75f, 1f, Colors.pack(1, 0, 0, 1), 0);
        VertexStream vertices = new VertexStream(VertexLayout.SPRITE, 4);
        build(list, vertices);

        ByteBuffer data = vertices.slice(4, 8); // second quad
        float[][] expected = {{10, 20, 0.25f, 0.5f}, {30, 20, 0.75f, 0.5f}, {30, 40, 0.75f, 1f}, {10, 40, 0.25f, 1f}};
        for (int v = 0; v < 4; v++) {
            int base = v * 24;
            assertEquals(expected[v][0], data.getFloat(base));
            assertEquals(expected[v][1], data.getFloat(base + 4));
            assertEquals(expected[v][2], data.getFloat(base + 8));
            assertEquals(expected[v][3], data.getFloat(base + 12));
            assertEquals((byte) 255, data.get(base + 16)); // R
            assertEquals((byte) 0, data.get(base + 17));   // G
            assertEquals((byte) 0, data.get(base + 18));   // B
            assertEquals((byte) 255, data.get(base + 19)); // A
            assertEquals(1, data.get(base + 20));          // slot of texture b
        }
    }

    @Test
    void emptyListProducesNoBatches() {
        DrawList list = new DrawList(LayerMode.SORTED);
        BatchList batches = build(list, new VertexStream(VertexLayout.SPRITE, 4));
        assertEquals(0, batches.size());
    }
}
