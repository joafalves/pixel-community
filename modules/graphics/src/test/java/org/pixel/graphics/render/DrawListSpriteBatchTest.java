package org.pixel.graphics.render;

import org.junit.jupiter.api.Test;
import org.pixel.commons.Color;
import org.pixel.content.Font;
import org.pixel.content.FontGlyph;
import org.pixel.content.Texture;
import org.pixel.graphics.draw.Colors;
import org.pixel.graphics.testing.RecordingBackend;
import org.pixel.graphics.testing.RecordingBackend.Vertex;
import org.pixel.graphics.testing.TestTexture;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DrawListSpriteBatchTest {

    private static final Matrix4 VIEW = new Matrix4();

    private static DrawListSpriteBatch batch(RecordingBackend backend) {
        DrawListSpriteBatch batch = new DrawListSpriteBatch(backend);
        assertTrue(batch.init());
        return batch;
    }

    /**
     * The vertex math of the former GLSpriteBatch (computeSpriteDataViewMatrix + transformMatrix4 of the unit quad).
     */
    private static float[] legacyCorners(float x, float y, float w, float h, float ax, float ay, float rotation) {
        Matrix4 m = new Matrix4();
        m.translate(x - w * ax, y - h * ay, 0);
        if (rotation != 0) {
            m.translate(w * ax, h * ay, 0);
            m.rotate(rotation, 0f, 0f, 1.0f);
            m.translate(-w * ax, -h * ay, 0);
        }
        m.scale(w, h, 0.0f);
        float[][] unit = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
        float[] out = new float[8];
        for (int i = 0; i < 4; i++) {
            Vector2 v = Vector2.transformMatrix4(unit[i][0], unit[i][1], m);
            out[i * 2] = v.getX();
            out[i * 2 + 1] = v.getY();
        }
        return out;
    }

    @Test
    void geometryMatchesTheClassicSpriteBatch() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        DrawListSpriteBatch batch = batch(backend);
        Texture texture = new TestTexture(64, 32);
        Random random = new Random(7);

        int n = 500;
        float[][] expected = new float[n][];
        batch.begin(VIEW);
        for (int i = 0; i < n; i++) {
            float x = random.nextFloat() * 1000 - 200, y = random.nextFloat() * 800 - 100;
            float ax = random.nextFloat(), ay = random.nextFloat();
            float sx = 0.25f + random.nextFloat() * 3, sy = 0.25f + random.nextFloat() * 3;
            float rotation = random.nextInt(3) == 0 ? 0f : (random.nextFloat() - 0.5f) * 12f;
            Rectangle source = random.nextBoolean() ? null : new Rectangle(8, 4, 16 + random.nextInt(40), 8);
            float w = source != null ? source.getWidth() * sx : texture.getWidth() * sx;
            float h = source != null ? source.getHeight() * sy : texture.getHeight() * sy;
            expected[i] = legacyCorners(x, y, w, h, ax, ay, rotation);
            batch.draw(texture, new Vector2(x, y), source, Color.WHITE, new Vector2(ax, ay), sx, sy, rotation, 0);
        }
        batch.end();

        List<Vertex> vertices = backend.lastPass().vertices();
        assertEquals(n * 4, vertices.size());
        for (int i = 0; i < n; i++) {
            for (int c = 0; c < 4; c++) {
                Vertex v = vertices.get(i * 4 + c);
                assertEquals(expected[i][c * 2], v.x(), 1e-3, "sprite " + i + " corner " + c + " x");
                assertEquals(expected[i][c * 2 + 1], v.y(), 1e-3, "sprite " + i + " corner " + c + " y");
            }
        }
    }

    @Test
    void displayAreaAndSourceRectangle() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        DrawListSpriteBatch batch = batch(backend);
        Texture texture = new TestTexture(100, 50);

        batch.begin(VIEW);
        batch.draw(texture, new Rectangle(10, 20, 30, 40), new Rectangle(25, 10, 50, 25), Color.RED);
        batch.end();

        List<Vertex> v = backend.lastPass().vertices();
        assertEquals(new Vertex(10, 20, 0.25f, 0.2f, Colors.pack(Color.RED), 0), v.get(0));
        assertEquals(new Vertex(40, 20, 0.75f, 0.2f, Colors.pack(Color.RED), 0), v.get(1));
        assertEquals(new Vertex(40, 60, 0.75f, 0.7f, Colors.pack(Color.RED), 0), v.get(2));
        assertEquals(new Vertex(10, 60, 0.25f, 0.7f, Colors.pack(Color.RED), 0), v.get(3));
    }

    @Test
    void depthOrderIsGlobalAndStable() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        DrawListSpriteBatch batch = batch(backend);
        Texture texture = new TestTexture(1, 1);
        Random random = new Random(3);
        int n = 2000; // well above the classic 256-sprite chunk
        int[] depth = new int[n];

        batch.begin(VIEW);
        for (int i = 0; i < n; i++) {
            depth[i] = random.nextInt(10);
            batch.draw(texture, new Vector2(i, 0), null, Color.WHITE, Vector2.ZERO, 1, 1, 0, depth[i]);
        }
        batch.end();

        List<Vertex> v = backend.lastPass().vertices();
        int previous = -1;
        for (int q = 0; q < n; q++) {
            int index = (int) v.get(q * 4).x(); // x encodes the submission index
            if (previous >= 0) {
                assertTrue(depth[previous] < depth[index] || (depth[previous] == depth[index] && previous < index),
                        "order broken at quad " + q);
            }
            previous = index;
        }
    }

    @Test
    void oneTextureNeedsOneBatchPerMaximumQuadCount() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        DrawListSpriteBatch batch = batch(backend);
        Texture texture = new TestTexture(4, 4);

        batch.begin(VIEW);
        for (int i = 0; i < 20_000; i++) {
            batch.draw(texture, new Vector2(i, i), Color.WHITE);
        }
        batch.end();

        assertEquals(2, backend.lastPass().batches().size()); // 16384 + 3616
    }

    @Test
    void textAndSpritesShareBatches() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        DrawListSpriteBatch batch = batch(backend);
        TestFont font = new TestFont();
        Texture texture = new TestTexture(4, 4);

        batch.begin(VIEW);
        for (int i = 0; i < 100; i++) {
            batch.draw(texture, new Vector2(i, 0), Color.WHITE);
            batch.drawText(font, "ab", new Vector2(i, 10), Color.WHITE);
        }
        batch.end();

        assertEquals(1, backend.lastPass().batches().size());
        assertEquals(300, backend.lastPass().batches().get(0).quadCount());
    }

    @Test
    void textPlacementMatchesTheClassicSpriteBatch() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        DrawListSpriteBatch batch = batch(backend);
        TestFont font = new TestFont(); // size 10, glyph 'a' 6x8 at (0,0) adv 7 off (1,-8); 'b' at (8,0) adv 9
        font.setHorizontalSpacing(1);
        font.setVerticalSpacing(2);

        batch.begin(VIEW);
        batch.drawText(font, "ab\na", new Vector2(100.7f, 50.2f), Color.WHITE, 20);
        batch.end();

        // scale 2; pen x = 100, baseline y = (int)(50.2 + 10*2 + 2) = 72; advance = (int)(7*2 + 1) = 15
        List<Vertex> v = backend.lastPass().vertices();
        assertEquals(12, v.size()); // 3 glyphs, '\n' skipped
        assertEquals(new Vertex(102, 56, 0f, 0f, Colors.pack(Color.WHITE), 0), v.get(0));   // 'a': 100+1*2, 72-8*2
        assertEquals(new Vertex(114, 72, 6 / 32f, 8 / 32f, Colors.pack(Color.WHITE), 0), v.get(2));
        assertEquals(117, v.get(4).x()); // 'b' at pen 115 + 1*2
        assertEquals(102, v.get(8).x()); // new line resets the pen
        assertEquals(56 + 22, v.get(8).y()); // line height (int)(10*2 + 2) = 22
    }

    @Test
    void blendModeAndBeginEndContract() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        DrawListSpriteBatch batch = batch(backend);
        Texture texture = new TestTexture(4, 4);

        assertThrows(IllegalStateException.class, () -> batch.draw(texture, Vector2.ZERO, Color.WHITE));
        assertThrows(IllegalStateException.class, batch::end);

        batch.begin(VIEW, BlendMode.ADDITIVE);
        assertThrows(IllegalStateException.class, () -> batch.begin(VIEW));
        batch.draw(texture, Vector2.ZERO, Color.WHITE);
        batch.end();
        assertEquals(BlendMode.ADDITIVE, backend.lastPass().blendMode());

        batch.begin(VIEW);
        batch.end(); // empty: nothing submitted
        assertEquals(1, backend.passes.size());

        batch.dispose();
        assertThrows(IllegalStateException.class, () -> batch.begin(VIEW));
    }

    @Test
    void steadyStateFramesDoNotAllocate() {
        RecordingBackend backend = new RecordingBackend(16, 16384, false);
        DrawListSpriteBatch batch = batch(backend);
        Texture[] textures = {new TestTexture(32, 32), new TestTexture(16, 16), new TestTexture(8, 8)};
        TestFont font = new TestFont();
        Vector2 position = new Vector2();
        Vector2 anchor = new Vector2(0.5f, 0.5f);
        Rectangle source = new Rectangle(0, 0, 8, 8);

        Runnable frame = () -> {
            batch.begin(VIEW);
            for (int i = 0; i < 20_000; i++) {
                position.set(i % 1000, i / 1000f);
                batch.draw(textures[i % 3], position, i % 2 == 0 ? null : source, Color.WHITE, anchor, 1, 1,
                        i * 0.01f, i % 7);
            }
            batch.drawText(font, "ab ab ab ab é", position, Color.WHITE); // é: glyph lookups beyond ASCII too
            batch.end();
        };
        for (int i = 0; i < 30; i++) {
            frame.run(); // warm-up: buffers grow to their steady size, JIT compiles
        }

        com.sun.management.ThreadMXBean threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long thread = Thread.currentThread().getId();
        long before = threads.getThreadAllocatedBytes(thread);
        for (int i = 0; i < 20; i++) {
            frame.run();
        }
        long allocated = threads.getThreadAllocatedBytes(thread) - before;

        assertEquals(0, allocated, "bytes allocated by 20 frames of 20k sprites");
        assertEquals(50, backend.passCount);
    }

    /** Bitmap font with two glyphs in a 32x32 atlas. */
    private static final class TestFont extends Font {
        private final Texture atlas = new TestTexture(32, 32);

        TestFont() {
            super(10, glyphs());
        }

        private static Map<Character, FontGlyph> glyphs() {
            Map<Character, FontGlyph> glyphs = new HashMap<>();
            glyphs.put('a', FontGlyph.builder().x(0).y(0).width(6).height(8).xAdvance(7).xOffset(1).yOffset(-8).build());
            glyphs.put('b', FontGlyph.builder().x(8).y(0).width(6).height(8).xAdvance(9).xOffset(1).yOffset(-8).build());
            glyphs.put(' ', FontGlyph.builder().x(16).y(0).width(0).height(0).xAdvance(4).xOffset(0).yOffset(0).build());
            return glyphs;
        }

        @Override
        public Texture getTexture() {
            return atlas;
        }

        @Override
        public void dispose() {
        }
    }
}
