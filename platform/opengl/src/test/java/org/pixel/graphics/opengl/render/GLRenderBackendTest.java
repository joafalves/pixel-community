package org.pixel.graphics.opengl.render;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pixel.commons.Color;
import org.pixel.content.opengl.GLTexture;
import org.pixel.graphics.RenderStats;
import org.pixel.graphics.backend.VertexLayout;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.backend.RenderTarget;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.material.Material;
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
        assertEquals(128L * 32 * Float.BYTES, styles.size()); // 128 styles of 8 vec4 (16 KB, the GLES 3.0 minimum)
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
    void passesIntoTargetsBindTheirFramebufferAndRestoreTheScreen() {
        RenderTarget target = backend.createTarget(256, 128);
        RenderPipeline.Builder builder = RenderPipeline.builder(backend);
        RenderPipeline pipeline = builder.pass(() -> WIDE, target, LAYER).build();
        pipeline.canvas(LAYER).rect(0, 0, 10, 10).fill(org.pixel.commons.Color.WHITE);
        gl.clear();
        pipeline.render();

        int fbo = ((GLRenderTarget) target).framebuffer();
        assertEquals(List.of("bind fb" + fbo, "viewport 256x128", "clear fb" + fbo, "bind fb0", "viewport 800x600"),
                gl.calls);
        assertEquals(1, gl.draws.size());
        assertEquals(fbo, gl.draws.get(0).framebuffer()); // drawn into the target
        assertEquals(0, gl.framebuffer);
        assertEquals(800, backend.getScreenWidth());
        pipeline.dispose();
        target.dispose();
    }

    @Test
    void postEffectsDrawAFullScreenTriangleIntoTheirDestination() {
        RenderPipeline.Builder builder = RenderPipeline.builder(backend);
        RenderTarget scene = builder.screenTarget(1f);
        RenderPipeline pipeline = builder.pass(() -> WIDE, scene, LAYER).post(scene,
                org.pixel.graphics.pass.Effects.sepia(1f)).build();
        gl.clear();
        pipeline.render();

        int fbo = ((GLRenderTarget) scene).framebuffer();
        assertTrue(gl.calls.contains("clear fb" + fbo));
        assertEquals("drawArrays fb0", gl.calls.stream().filter(c -> c.startsWith("drawArrays")).findFirst()
                .orElseThrow()); // the effect writes the screen
        assertEquals(800, scene.getWidth()); // screen-sized
        assertTrue(gl.shaderSources.stream().anyMatch(src -> src.contains("vec4 effect(vec2 uv)")
                && src.contains("oFragColor = effect(vUv);")));
        pipeline.dispose();
    }

    @Test
    void targetsMadeBeforeAContextLossAreAllocatedAgainWithoutDeletingDeadNames() {
        RenderTarget target = backend.createTarget(64, 32);
        int before = ((GLRenderTarget) target).framebuffer();
        backend.contextRecreated();
        assertEquals(1, backend.getContextGeneration());
        gl.clear();
        backend.readPixels(target);
        int after = ((GLRenderTarget) target).framebuffer();
        assertNotEquals(before, after);
        assertEquals(64, target.getWidth());
        assertTrue(gl.calls.contains("bind fb" + after));
        target.dispose();
    }

    @Test
    void readPixelsReadsTheTargetAndRestoresTheFramebuffer() {
        RenderTarget target = backend.createTarget(4, 2);
        gl.clear();
        assertEquals(4 * 2 * 4, backend.readPixels(target).length);
        int fbo = ((GLRenderTarget) target).framebuffer();
        assertEquals(List.of("bind fb" + fbo, "read fb" + fbo, "bind fb0"), gl.calls);
        target.dispose();
        assertThrows(IllegalArgumentException.class, () -> backend.readPixels(target)); // disposed
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
        Material flash = Material.effect("uniform float uTime;\n"
                + "vec4 effect(vec4 color, vec2 uv, Fragment f) { return color * f.data.x; }");
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
            flash.set("uTime", position.getX()); // changes every frame: uploaded every frame
            for (int i = 0; i < 100; i++) {
                c.image(countingTexture, i, 0).material(flash, i % 4); // per-command data, one batch
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
        assertEquals(40 * 4, counting.drawCount); // per frame: 20,000 sprites in 2 chunks, shapes, material images
        flash.dispose();
        sprites.dispose();
        pipeline.dispose();
        countingBackend.dispose();
    }

    // region materials

    private static final String TINT = "uniform vec4 uTint;\nuniform sampler2D uNoise;\n"
            + "vec4 effect(vec4 color, vec2 uv, Fragment f) { return color * uTint * texture(uNoise, uv).r; }";

    @Test
    void materialBatchesUseTheMaterialProgramWithItsValuesAndTextures() {
        GLTexture noise = new GLTexture(gl, 3000, 8, 8);
        Material tint = Material.effect(TINT).set("uTint", 1f, 0.5f, 0.25f, 1f).texture("uNoise", noise);
        RenderPipeline pipeline = RenderPipeline.builder(backend).pass(() -> WIDE, LAYER).build();
        Canvas c = pipeline.canvas(LAYER);
        c.rect(0, 0, 10, 10).fill(Color.RED);
        c.image(texture, 0, 0).material(tint, 0.5f);
        c.image(texture, 20, 0).material(tint, 0.25f); // other data: same batch
        c.rect(0, 0, 10, 10).fill(Color.RED);
        int shaders = gl.shaderSources.size();
        pipeline.render();

        String fragment = gl.shaderSources.get(shaders + 1); // the material's vertex + fragment shader were compiled
        assertEquals(shaders + 2, gl.shaderSources.size());
        assertTrue(fragment.contains("#define PIXEL_MATERIAL") && fragment.contains("uniform vec4 uTint;"));
        assertTrue(fragment.contains("uniform sampler2D uTextures[12];")); // 16 units, 4 kept for material textures
        assertEquals(3, gl.draws.size());
        int builtIn = gl.draws.get(0).program();
        assertNotEquals(builtIn, gl.draws.get(1).program());
        assertEquals(builtIn, gl.draws.get(2).program());
        assertTrue(gl.uniforms.contains("uTint=1.0,0.5,0.25,1.0"));
        assertTrue(gl.uniforms.contains("uNoise=12")); // the first unit after the command textures
        assertEquals(3000, gl.unitTextures.get(12));

        gl.uniforms.clear();
        c.image(texture, 0, 0).material(tint);
        pipeline.render();
        assertFalse(gl.uniforms.stream().anyMatch(u -> u.startsWith("uTint")), "unchanged values are not uploaded");
        tint.set("uTint", Color.WHITE);
        c.image(texture, 0, 0).material(tint);
        pipeline.render();
        assertTrue(gl.uniforms.contains("uTint=1.0,1.0,1.0,1.0"));
        assertEquals(shaders + 2, gl.shaderSources.size(), "compiled once");
        tint.dispose();
        pipeline.dispose();
    }

    @Test
    void aMaterialThatFailsToReloadKeepsItsPreviousProgram() {
        Material tint = Material.effect(TINT).set("uTint", Color.WHITE);
        RenderPipeline pipeline = RenderPipeline.builder(backend).pass(() -> WIDE, LAYER).build();
        Canvas c = pipeline.canvas(LAYER);
        c.image(texture, 0, 0).material(tint);
        pipeline.render();
        int program = gl.draws.get(0).program();

        gl.failCompile = "oops";
        tint.setSource(TINT + " oops");
        gl.draws.clear();
        c.image(texture, 0, 0).material(tint);
        pipeline.render();
        assertEquals(program, gl.draws.get(0).program());
        assertTrue(tint.getError().contains("oops"));
        assertFalse(gl.deletedPrograms.contains(program));

        gl.failCompile = null;
        tint.setSource(TINT + " // fixed");
        gl.draws.clear();
        c.image(texture, 0, 0).material(tint);
        pipeline.render();
        assertNotEquals(program, gl.draws.get(0).program());
        assertNull(tint.getError());
        assertTrue(gl.deletedPrograms.contains(program), "the replaced program is deleted");
        tint.dispose();
        pipeline.dispose();
    }

    @Test
    void materialsCompileAgainInARecreatedContext() {
        Material tint = Material.effect(TINT).set("uTint", Color.WHITE);
        RenderPipeline pipeline = RenderPipeline.builder(backend).pass(() -> WIDE, LAYER).build();
        Canvas c = pipeline.canvas(LAYER);
        c.image(texture, 0, 0).material(tint);
        pipeline.render();
        int program = gl.draws.get(0).program();

        backend.contextRecreated();
        gl.draws.clear();
        gl.uniforms.clear();
        c.image(texture, 0, 0).material(tint);
        pipeline.render();
        assertNotEquals(program, gl.draws.get(0).program());
        assertFalse(gl.deletedPrograms.contains(program), "names of the lost context are never deleted");
        assertTrue(gl.uniforms.contains("uTint=1.0,1.0,1.0,1.0"), "values are uploaded to the new program");
        tint.dispose();
        pipeline.dispose();
    }

    @Test
    void rawMaterialsGetTheSlotHelpersAndOptionalBlocks() {
        Material raw = Material.raw("void main() { gl_Position = vec4(0.0); }",
                "out vec4 o; void main() { o = pixelSample(0, vec2(0.0)); }");
        RenderPipeline pipeline = RenderPipeline.builder(backend).pass(() -> WIDE, LAYER).build();
        pipeline.canvas(LAYER).image(texture, 0, 0).material(raw);
        int shaders = gl.shaderSources.size();
        pipeline.render();
        String fragment = gl.shaderSources.get(shaders + 1);
        assertTrue(fragment.contains("#define PIXEL_TEXTURE_SLOTS 12"));
        assertTrue(fragment.contains("vec4 pixelSample(int slot, vec2 uv)"));
        assertEquals(1, gl.draws.size());
        raw.dispose();
        pipeline.dispose();
    }

    // endregion
}
