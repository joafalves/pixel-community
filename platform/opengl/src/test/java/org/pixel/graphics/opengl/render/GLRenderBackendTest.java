package org.pixel.graphics.opengl.render;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pixel.commons.Color;
import org.pixel.content.opengl.GLTexture;
import org.pixel.graphics.RenderStats;
import org.pixel.graphics.backend.VertexLayout;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.pass.RenderPipeline;
import org.pixel.graphics.opengl.GLApi;
import org.pixel.graphics.render.DrawListSpriteBatch;
import org.pixel.math.Matrix4;
import org.pixel.math.Vector2;

import java.lang.management.ManagementFactory;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The shared GL backend against a recording {@link GLApi} (no GPU): what is uploaded and drawn, and that steady
 * frames allocate nothing.
 */
class GLRenderBackendTest {

    private static final int SPRITE_QUAD_BYTES = 4 * VertexLayout.SPRITE.stride();

    private final FakeGLApi gl = new FakeGLApi(true);
    private final RenderStats stats = new RenderStats();
    private final GLRenderBackend backend = new GLRenderBackend(gl, stats);
    private final GLTexture texture = new GLTexture(gl, 1000, 32, 32);
    private final Matrix4 view = new Matrix4();
    private static final Matrix4 WIDE = Matrix4.orthographic(-1e5f, 1e5f, 1e5f, -1e5f, 0f, 1f); // nothing culled
    private static final Layer LAYER = Layer.ordered("test");

    @AfterEach
    void dispose() {
        backend.dispose();
    }

    private void drawSprites(DrawListSpriteBatch batch, int count) {
        batch.begin(view);
        Vector2 position = new Vector2();
        for (int i = 0; i < count; i++) {
            position.set(i % 100, i / 100f);
            batch.draw(texture, position);
        }
        batch.end();
    }

    @Test
    void shadersGetThePlatformPreambleAndNoTemplateMarkers() {
        assertEquals(4, gl.shaderSources.size()); // sprite + shape, vertex + fragment
        for (String source : gl.shaderSources) {
            assertTrue(source.startsWith("#version 300 es\n"));
            assertFalse(source.contains("/*$"), "unreplaced template marker");
        }
        assertEquals(16, backend.capabilities().maxTextureSlots());
        assertEquals(64, backend.capabilities().tableAlignmentFloats()); // 256 bytes
    }

    @Test
    void spritesAreUploadedOnceAndDrawnIndexed() {
        DrawListSpriteBatch batch = new DrawListSpriteBatch(backend);
        batch.init();
        drawSprites(batch, 10);

        List<FakeGLApi.Upload> vertexUploads = gl.uploads.stream()
                .filter(u -> u.target() == GLApi.GL_ARRAY_BUFFER).toList();
        assertEquals(List.of(new FakeGLApi.Upload(GLApi.GL_ARRAY_BUFFER, vertexUploads.get(0).buffer(), 0,
                10 * SPRITE_QUAD_BYTES)), vertexUploads);
        assertEquals(1, gl.draws.size());
        assertEquals(60, gl.draws.get(0).count());
        assertEquals(0, gl.draws.get(0).offset());
        batch.dispose();
    }

    @Test
    void batchesBeyondSixteenBitIndicesAreUploadedInChunks() {
        DrawListSpriteBatch batch = new DrawListSpriteBatch(backend);
        batch.init();
        int max = GLRenderBackend.MAX_QUADS_PER_BATCH;
        drawSprites(batch, max + 100);

        List<FakeGLApi.Upload> uploads = gl.uploads;
        assertEquals(2, uploads.size());
        assertEquals(0, uploads.get(0).offset());
        assertEquals(max * SPRITE_QUAD_BYTES, uploads.get(0).size());
        assertEquals(max * SPRITE_QUAD_BYTES, uploads.get(1).offset()); // second chunk starts where the first ended
        assertEquals(100 * SPRITE_QUAD_BYTES, uploads.get(1).size());
        assertEquals(2, gl.draws.size());
        assertEquals(max * 6, gl.draws.get(0).count());
        assertEquals(0, gl.draws.get(1).offset()); // index offsets are relative to the chunk: no base vertex needed
        assertEquals(600, gl.draws.get(1).count());
        batch.dispose();
    }

    @Test
    void shapeBatchesBindTheirSlicesOfTheClipAndStyleTables() {
        RenderPipeline pipeline = RenderPipeline.builder(backend).pass(() -> WIDE, LAYER).build();
        Canvas c = pipeline.canvas(LAYER);
        c.clip(0, 0, 50, 50);
        c.rect(0, 0, 10, 10).fill(Color.RED).stroke(2, Color.WHITE);
        pipeline.render();

        assertEquals(1, gl.draws.size());
        assertEquals(2, gl.ranges.size());
        FakeGLApi.Range clips = gl.ranges.get(0), styles = gl.ranges.get(1);
        assertEquals(GLShapeProgram.CLIP_BINDING, clips.index());
        assertEquals(GLShapeProgram.STYLE_BINDING, styles.index());
        assertEquals(0, clips.offset());
        assertEquals(256L * 4 * Float.BYTES, clips.size());   // 256 clips of one vec4
        assertEquals(128L * 28 * Float.BYTES, styles.size()); // 128 styles of 7 vec4
        assertNotEquals(gl.draws.get(0).program(), 0);
        pipeline.dispose();
    }

    @Test
    void recordsDrawCallsAndUploadsInTheDeviceStats() {
        DrawListSpriteBatch batch = new DrawListSpriteBatch(backend);
        batch.init();
        drawSprites(batch, 10);
        stats.endFrame();
        assertEquals(1, stats.lastFrame().getDrawCalls());
        assertEquals(10, stats.lastFrame().getPrimitives());
        assertEquals(10L * SPRITE_QUAD_BYTES, stats.lastFrame().getUploadedBytes());
        batch.dispose();
    }

    @Test
    void contextRecreationRebuildsTheGlObjectsInPlace() {
        DrawListSpriteBatch batch = new DrawListSpriteBatch(backend);
        batch.init();
        drawSprites(batch, 10);
        FakeGLApi.Draw before = gl.draws.get(0);

        gl.clear();
        gl.shaderSources.clear();
        backend.contextRecreated(); // e.g. Android dropped the context while paused
        drawSprites(batch, 10);     // the same batch keeps working

        assertEquals(4, gl.shaderSources.size()); // both programs compiled again
        assertEquals(1, gl.draws.size());
        assertNotEquals(before.program(), gl.draws.get(0).program());
        assertNotEquals(before.vertexArray(), gl.draws.get(0).vertexArray());
        batch.dispose();
    }

    @Test
    void steadyStateFramesDoNotAllocate() {
        FakeGLApi counting = new FakeGLApi(false);
        GLRenderBackend countingBackend = new GLRenderBackend(counting, new RenderStats());
        GLTexture countingTexture = new GLTexture(counting, 2000, 32, 32);
        DrawListSpriteBatch sprites = new DrawListSpriteBatch(countingBackend);
        sprites.init();
        RenderPipeline pipeline = RenderPipeline.builder(countingBackend).pass(() -> WIDE, LAYER).build();
        Vector2 position = new Vector2();
        Runnable frame = () -> {
            sprites.begin(view);
            for (int i = 0; i < 20_000; i++) {
                position.set(i % 100, i / 100f);
                sprites.draw(countingTexture, position);
            }
            sprites.end();
            Canvas c = pipeline.canvas(LAYER);
            for (int i = 0; i < 500; i++) {
                c.roundRect(i, i, 20, 10, 3).fill(Color.RED).stroke(1, Color.WHITE).shadow(0, 2, 4, Color.BLACK);
            }
            pipeline.render();
        };
        for (int i = 0; i < 20; i++) {
            frame.run();
        }

        com.sun.management.ThreadMXBean threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long thread = Thread.currentThread().getId();
        long before = threads.getThreadAllocatedBytes(thread);
        for (int i = 0; i < 20; i++) {
            frame.run();
        }
        assertEquals(0, threads.getThreadAllocatedBytes(thread) - before, "bytes allocated by 20 frames");
        assertEquals(40 * 3, counting.drawCount); // per frame: 20,000 sprites in 2 chunks + 1 shape batch
        sprites.dispose();
        pipeline.dispose();
        countingBackend.dispose();
    }
}
