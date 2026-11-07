package org.pixel.graphics.draw;

import org.junit.jupiter.api.Test;
import org.pixel.content.Texture;
import org.pixel.graphics.backend.BackendCapabilities;
import org.pixel.graphics.backend.BatchList;
import org.pixel.graphics.backend.RenderData;
import org.pixel.graphics.backend.VertexLayout;
import org.pixel.graphics.backend.VertexStream;
import org.pixel.graphics.testing.TestTexture;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

class BatcherTest {

    // 2 texture slots, 3 quads, 2 clips, 2 styles per batch; tables aligned to 8 floats
    private static final BackendCapabilities CAPS = new BackendCapabilities(2, 3, 2, 2, 8);

    private final Texture a = new TestTexture(8, 8);
    private final Texture b = new TestTexture(8, 8);
    private final Texture c = new TestTexture(8, 8);

    private static void quad(DrawList list, Texture texture) {
        list.addQuad(texture, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0xFFFFFFFF, 0);
    }

    private static void shape(DrawList list, int styleId, int clipId) {
        list.addShape(ShapeType.RECT, null, 0, 0, 1, 0, 1, 1, 0, 1, 5, 6, 5, 6, 7, 8, 1, 2, 3, 4, 0xFF0000FF,
                styleId, clipId, 0);
    }

    /** A style record with only a stroke (color as r, g, b), width and offset set. */
    private static float[] stroke(float r, float g, float b, float width, float offset) {
        float[] record = new float[StyleTable.FLOATS];
        record[StyleTable.STROKE_COLOR] = r;
        record[StyleTable.STROKE_COLOR + 1] = g;
        record[StyleTable.STROKE_COLOR + 2] = b;
        record[StyleTable.STROKE_COLOR + 3] = 1f;
        record[StyleTable.STROKE_WIDTH] = width;
        record[StyleTable.STROKE_OFFSET] = offset;
        return record;
    }

    private static RenderData build(DrawList list) {
        RenderData data = new RenderData(CAPS.maxTextureSlots());
        list.prepare();
        new Batcher().build(list, CAPS, data);
        return data;
    }

    @Test
    void consecutiveQuadsShareABatchUntilTheQuadLimit() {
        DrawList list = new DrawList(LayerMode.ORDERED);
        for (int i = 0; i < 7; i++) quad(list, a);
        RenderData data = build(list);
        BatchList batches = data.batches();

        assertEquals(3, batches.size()); // 3 + 3 + 1
        assertEquals(0, batches.firstQuad(0));
        assertEquals(3, batches.firstQuad(1));
        assertEquals(6, batches.firstQuad(2));
        assertEquals(1, batches.quadCount(2));
        assertEquals(28, data.vertices(VertexLayout.SPRITE).vertexCount());
    }

    @Test
    void texturesShareABatchWhileSlotsAreFree() {
        DrawList list = new DrawList(LayerMode.ORDERED);
        quad(list, a);
        quad(list, b);
        quad(list, a); // a and b fit in 2 slots
        BatchList batches = build(list).batches();

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
        BatchList batches = build(list).batches();

        assertEquals(2, batches.size());
        assertEquals(1, batches.quadCount(1));
        assertSame(c, batches.texture(1, 0));
    }

    @Test
    void spriteVerticesCarryPositionsTexCoordsColorAndSlot() {
        DrawList list = new DrawList(LayerMode.ORDERED);
        quad(list, a);
        list.addQuad(b, 10, 20, 30, 20, 30, 40, 10, 40, 0.25f, 0.5f, 0.75f, 1f, Colors.pack(1, 0, 0, 1), 0);
        VertexStream vertices = build(list).vertices(VertexLayout.SPRITE);

        ByteBuffer data = vertices.slice(4, 8); // second quad
        float[][] expected = {{10, 20, 0.25f, 0.5f}, {30, 20, 0.75f, 0.5f}, {30, 40, 0.75f, 1f}, {10, 40, 0.25f, 1f}};
        for (int v = 0; v < 4; v++) {
            int base = v * VertexLayout.SPRITE.stride();
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
    void layoutChangesBreakBatches() {
        DrawList list = new DrawList(LayerMode.ORDERED);
        quad(list, a);
        shape(list, 0, 0);
        shape(list, 0, 0);
        quad(list, a);
        RenderData data = build(list);
        BatchList batches = data.batches();

        assertEquals(3, batches.size());
        assertEquals(VertexLayout.SPRITE, batches.layout(0));
        assertEquals(VertexLayout.FULL, batches.layout(1));
        assertEquals(0, batches.firstQuad(1)); // first quad of the FULL stream
        assertEquals(2, batches.quadCount(1));
        assertEquals(VertexLayout.SPRITE, batches.layout(2));
        assertEquals(1, batches.firstQuad(2)); // second quad of the SPRITE stream
        assertEquals(2, data.vertices(VertexLayout.SPRITE).quadCount());
        assertEquals(2, data.vertices(VertexLayout.FULL).quadCount());
    }

    @Test
    void stylesAndClipsAreRemappedPerBatchAndAligned() {
        DrawList list = new DrawList(LayerMode.ORDERED);
        int s1 = list.styles().intern(stroke(0, 1, 0, 2f, 0f));
        int s2 = list.styles().intern(stroke(1, 0, 0, 3f, -1.5f));
        int s3 = list.styles().intern(stroke(0, 0, 1, 4f, 2f));
        int c1 = list.clips().add(0, 0, 10, 10);
        shape(list, s1, c1);
        shape(list, s2, c1);
        shape(list, s1, 0);  // reuses batch-local style 1 (and the batch is now full: 3 quads)
        shape(list, s3, c1); // new batch with its own tables
        RenderData data = build(list);
        BatchList batches = data.batches();

        assertEquals(2, batches.size());
        assertEquals(3, batches.quadCount(0));
        assertEquals(2, batches.styleCount(0));
        assertEquals(1, batches.clipCount(0));
        assertEquals(0, batches.styleOffset(1) % CAPS.tableAlignmentFloats());
        assertEquals(0, batches.clipOffset(1) % CAPS.tableAlignmentFloats());
        assertEquals(1, batches.styleCount(1));
        assertEquals(1, batches.clipCount(1));

        ByteBuffer v = data.vertices(VertexLayout.FULL).slice(0, 16);
        int stride = VertexLayout.FULL.stride();
        assertEquals(1, v.getShort(46) & 0xFFFF);               // quad 0: clip 1
        assertEquals(1, v.getShort(48) & 0xFFFF);               // quad 0: style 1
        assertEquals(2, v.getShort(4 * stride + 48) & 0xFFFF);  // quad 1: style 2
        assertEquals(0, v.getShort(8 * stride + 46) & 0xFFFF);  // quad 2: no clip
        assertEquals(1, v.getShort(8 * stride + 48) & 0xFFFF);  // quad 2: style 1 again
        assertEquals(1, v.getShort(12 * stride + 48) & 0xFFFF); // quad 3: style 1 of batch 2

        int o = batches.styleOffset(0) + BatchList.STYLE_FLOATS; // style 2 of batch 0 (red, width 3, offset -1.5)
        assertEquals(1f, batches.styleData()[o + StyleTable.STROKE_COLOR]);
        assertEquals(0f, batches.styleData()[o + StyleTable.STROKE_COLOR + 1]);
        assertEquals(3f, batches.styleData()[o + StyleTable.STROKE_WIDTH]);
        assertEquals(-1.5f, batches.styleData()[o + StyleTable.STROKE_OFFSET]);
        int c = batches.clipOffset(1);
        assertArrayEquals(new float[]{0, 0, 10, 10}, java.util.Arrays.copyOfRange(batches.clipData(), c, c + 4));
    }

    @Test
    void aFullStyleTableStartsANewBatch() {
        DrawList list = new DrawList(LayerMode.ORDERED);
        int s1 = list.styles().intern(stroke(1, 0, 0, 1f, 0f));
        int s2 = list.styles().intern(stroke(0, 1, 0, 1f, 0f));
        int s3 = list.styles().intern(stroke(0, 0, 1, 1f, 0f));
        shape(list, s1, 0);
        shape(list, s2, 0);
        shape(list, s3, 0);
        BatchList batches = build(list).batches();

        assertEquals(2, batches.size());
        assertEquals(2, batches.quadCount(0));
        assertEquals(1, batches.quadCount(1));
    }

    @Test
    void fullVerticesCarryLocalCoordinatesAndParameters() {
        DrawList list = new DrawList(LayerMode.ORDERED);
        shape(list, 0, 0);
        ByteBuffer v = build(list).vertices(VertexLayout.FULL).slice(0, 4);
        int stride = VertexLayout.FULL.stride();
        float[][] local = {{-7, -8}, {7, -8}, {7, 8}, {-7, 8}};
        for (int i = 0; i < 4; i++) {
            int base = i * stride;
            assertEquals(5f, v.getFloat(base + 8));  // dimension x (u)
            assertEquals(6f, v.getFloat(base + 12)); // dimension y (v)
            assertEquals(local[i][0], v.getFloat(base + 20));
            assertEquals(local[i][1], v.getFloat(base + 24));
            assertEquals(1f, v.getFloat(base + 28));
            assertEquals(4f, v.getFloat(base + 40));
            assertEquals(ShapeType.RECT.code(), v.get(base + 44));
        }
    }

    @Test
    void emptyListProducesNoBatches() {
        DrawList list = new DrawList(LayerMode.SORTED);
        assertEquals(0, build(list).batches().size());
    }
}
