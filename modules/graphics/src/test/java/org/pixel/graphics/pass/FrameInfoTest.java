package org.pixel.graphics.pass;

import org.junit.jupiter.api.Test;
import org.pixel.commons.Color;
import org.pixel.content.Texture;
import org.pixel.graphics.backend.BackendCapabilities;
import org.pixel.graphics.backend.RenderTarget;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.canvas.LineCap;
import org.pixel.graphics.canvas.StrokeAlign;
import org.pixel.graphics.draw.BatchBreak;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.material.Material;
import org.pixel.graphics.testing.RecordingBackend;
import org.pixel.graphics.testing.TestFonts;
import org.pixel.graphics.testing.TestTexture;
import org.pixel.math.Matrix4;

import java.lang.management.ManagementFactory;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The last frame's statistics, batch list and picking.
 */
class FrameInfoTest {

    private static final View SCREEN = view(0, 0, 800, 600);
    private final Layer world = Layer.sorted("world");
    private final Layer hud = Layer.ordered("hud");
    private final Texture a = new TestTexture(8, 8), b = new TestTexture(8, 8), c = new TestTexture(8, 8);

    private static View view(float x0, float y0, float x1, float y1) {
        Matrix4 matrix = Matrix4.orthographic(x0, x1, y1, y0, 0f, 1f);
        return new View() {
            @Override
            public Matrix4 getViewMatrix() {
                return matrix;
            }

            @Override
            public float getPixelsPerUnit() {
                return 1f;
            }
        };
    }

    private RenderPipeline single(RecordingBackend backend, Layer layer) {
        return RenderPipeline.builder(backend).pass(SCREEN, layer).build();
    }

    // region stats

    @Test
    void layerStatsCountCommandsCullingBatchesAndBreakReasons() {
        // 2 texture slots, 100 quads, 2 clips, 2 styles per batch
        RecordingBackend backend = new RecordingBackend(new BackendCapabilities(2, 100, 2, 2, 8), true);
        RenderPipeline pipeline = single(backend, hud);
        Material m = Material.effect("vec4 effect(vec4 color, vec2 uv, Fragment f) { return color; }");
        Canvas canvas = pipeline.canvas(hud);
        canvas.image(a, 0, 0);
        canvas.image(b, 10, 0);
        canvas.image(c, 20, 0);                  // 3rd texture: TEXTURE_SLOTS
        canvas.image(c, 30, 0).material(m);      // MATERIAL
        canvas.rect(0, 0, 5, 5).stroke(1, Color.RED);
        canvas.rect(0, 0, 5, 5).stroke(2, Color.RED);
        canvas.rect(0, 0, 5, 5).stroke(3, Color.RED); // MATERIAL back to built-in, then 3rd style: STYLE_TABLE
        canvas.image(a, 5000, 5000);             // culled
        pipeline.render();

        FrameInfo frame = pipeline.lastFrame();
        assertEquals(1, frame.getFrameNumber());
        FrameInfo.LayerStats stats = frame.getLayers()[0];
        assertSame(hud, stats.getLayer());
        assertEquals(8, stats.getCommands());
        assertEquals(1, stats.getCulled());
        assertEquals(5, stats.getBatches());
        assertEquals(7, stats.getQuads());
        assertEquals(1, stats.getBreaks(BatchBreak.FIRST));
        assertEquals(1, stats.getBreaks(BatchBreak.TEXTURE_SLOTS));
        assertEquals(2, stats.getBreaks(BatchBreak.MATERIAL));
        assertEquals(1, stats.getBreaks(BatchBreak.STYLE_TABLE));
        assertEquals(5, stats.getDrawCalls());
        assertEquals(5, frame.getBatchCount());
        assertEquals(BatchBreak.MATERIAL, frame.getBatchBreak(2));
        assertSame(m, frame.getBatchMaterial(2));
        assertEquals(1, frame.getBatchQuads(2));
        assertEquals(2, frame.getBatchTextures(0));
        assertSame(stats, frame.getBatchLayer(4));
        assertTrue(frame.toString().contains("1 texture slots full"), frame.toString());

        pipeline.render(); // nothing recorded: everything back to zero
        assertEquals(2, frame.getFrameNumber());
        assertEquals(0, frame.getBatchCount());
        assertEquals(0, stats.getCommands());
        assertThrows(IndexOutOfBoundsException.class, () -> frame.getBatchMaterial(2), "last frame's batches are gone");
        pipeline.dispose();
    }

    @Test
    void passStatsCountDrawCallsOfEveryKindOfPass() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        RenderPipeline.Builder builder = RenderPipeline.builder(backend);
        RenderTarget scene = builder.screenTarget(1f);
        RenderPipeline pipeline = builder
                .pass(SCREEN, scene, world, hud)
                .post(scene, Effects.bloom(0.5f, 1f), Effects.vignette(0.5f))
                .custom(be -> {
                })
                .build();
        pipeline.canvas(world).rect(0, 0, 10, 10);
        pipeline.canvas(hud).image(a, 0, 0);
        pipeline.render();

        FrameInfo.PassStats[] passes = pipeline.lastFrame().getPasses();
        assertEquals(3, passes.length);
        assertEquals(FrameInfo.PassKind.LAYERS, passes[0].getKind());
        assertSame(scene, passes[0].getTarget());
        assertEquals(2, passes[0].getLayers().length);
        assertEquals(2, passes[0].getDrawCalls());
        assertEquals(FrameInfo.PassKind.POST, passes[1].getKind());
        assertEquals(7, passes[1].getDrawCalls()); // bloom: bright, 2 x (blur H + V), combine; vignette
        assertEquals(FrameInfo.PassKind.CUSTOM, passes[2].getKind());
        assertEquals(0, passes[2].getDrawCalls());
        assertEquals(9, pipeline.lastFrame().getDrawCalls());
        assertTrue(passes[1].getNanos() > 0 && pipeline.lastFrame().getNanos() >= passes[1].getNanos());
        pipeline.dispose();
    }

    // endregion

    // region picking

    @Test
    void shapesAreHitInsideTheirEdgeOrStrokeOnly() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        RenderPipeline pipeline = single(backend, hud);
        Canvas canvas = pipeline.canvas(hud);
        canvas.roundRect(100, 100, 100, 60, 20).pick(1);
        canvas.circle(400, 130, 40).noFill().stroke(10, Color.WHITE).strokeAlign(StrokeAlign.OUTSIDE).pick(2);
        canvas.line(100, 300, 300, 300).width(10).cap(LineCap.ROUND).pick(3);
        canvas.ellipse(600, 130, 80, 20).pick(4);
        pipeline.render();
        FrameInfo frame = pipeline.lastFrame();

        assertEquals(1, frame.pick(hud, 150, 130));
        assertEquals(1, frame.pick(hud, 104, 130)); // left edge, away from the corners
        assertEquals(0, frame.pick(hud, 102, 102), "outside the rounded corner, inside the rectangle");
        assertEquals(2, frame.pick(hud, 400, 130), "an unfilled shape is picked by its whole area");
        assertEquals(2, frame.pick(hud, 447, 130), "outside stroke: 40 + 10");
        assertEquals(0, frame.pick(hud, 452, 130));
        assertEquals(3, frame.pick(hud, 97, 300), "round cap");
        assertEquals(0, frame.pick(hud, 200, 306));
        assertEquals(4, frame.pick(hud, 670, 130));
        assertEquals(0, frame.pick(hud, 670, 145), "outside the ellipse, inside its quad");
        assertEquals(0, frame.pick(hud, 50, 50));
        pipeline.dispose();
    }

    @Test
    void pathsTextImagesAndClipsArePickedLikeTheyAreDrawn() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        RenderPipeline pipeline = single(backend, hud);
        Canvas canvas = pipeline.canvas(hud);
        // a "V": the notch between its arms is outside the fill
        canvas.path().moveTo(100, 100).lineTo(150, 200).lineTo(200, 100).lineTo(150, 150).close()
                .fill(Color.WHITE).pick(1);
        canvas.path().moveTo(300, 100).lineTo(400, 100).stroke(6, Color.WHITE).pick(2);
        canvas.text(TestFonts.mono(), "AB", 500, 100).size(20).pick(3);
        canvas.save();
        canvas.translate(150, 400);
        canvas.rotate((float) Math.PI / 4f);
        canvas.image(a, 0, 0).size(40, 40).anchor(0.5f, 0.5f).pick(4);
        canvas.restore();
        canvas.clip(300, 300, 50, 50);
        canvas.rect(300, 300, 200, 200).pick(5);
        pipeline.render();
        FrameInfo frame = pipeline.lastFrame();

        assertEquals(1, frame.pick(hud, 150, 180));
        assertEquals(0, frame.pick(hud, 150, 120), "inside the notch");
        assertEquals(2, frame.pick(hud, 350, 102));
        assertEquals(0, frame.pick(hud, 350, 106));
        assertEquals(3, frame.pick(hud, 505, 110));
        assertEquals(4, frame.pick(hud, 150, 375), "inside the rotated square's top corner");
        assertEquals(0, frame.pick(hud, 135, 385), "inside its bounding box only");
        assertEquals(5, frame.pick(hud, 320, 320));
        assertEquals(0, frame.pick(hud, 400, 400), "clipped away");
        pipeline.dispose();
    }

    @Test
    void theTopMostCommandWinsAndPickAllListsTheRest() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        RenderPipeline pipeline = single(backend, world);
        Canvas canvas = pipeline.canvas(world);
        canvas.rect(0, 0, 100, 100).depth(5).pick(1);   // drawn last: on top
        canvas.rect(0, 0, 100, 100).depth(1).pick(2);
        canvas.rect(0, 0, 100, 100).depth(3);           // not pickable
        canvas.text(TestFonts.mono(), "AA", 10, 10).size(20).stroke(1, Color.RED).shadow(2, 2, 2, Color.BLACK)
                .depth(2).pick(3);                     // several quads per glyph: listed once
        pipeline.render();
        FrameInfo frame = pipeline.lastFrame();

        assertEquals(1, frame.pick(world, 15, 15));
        int[] ids = new int[8];
        assertEquals(3, frame.pickAll(world, 15, 15, ids));
        assertArrayEquals(new int[]{1, 3, 2}, Arrays.copyOf(ids, 3));
        assertEquals(1, frame.pickAll(world, 15, 15, new int[1]));
        assertThrows(IllegalArgumentException.class, () -> frame.pick(Layer.ordered("other"), 0, 0));
        pipeline.dispose();
    }

    @Test
    void picksStayUntilTheNextFrameAndBuildersForgetTheirId() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        RenderPipeline pipeline = single(backend, hud);
        Canvas canvas = pipeline.canvas(hud);
        canvas.rect(0, 0, 10, 10).pick(7);
        pipeline.render();
        assertEquals(7, pipeline.lastFrame().pick(hud, 5, 5), "queryable after render(), while the next is recorded");
        canvas.rect(0, 0, 10, 10); // the same builder object, no pick id
        assertEquals(7, pipeline.lastFrame().pick(hud, 5, 5));
        pipeline.render();
        assertEquals(0, pipeline.lastFrame().pick(hud, 5, 5));
        pipeline.dispose();
    }

    @Test
    void dashesArePickedOnTheirDashesOnly() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        RenderPipeline pipeline = single(backend, hud);
        Canvas canvas = pipeline.canvas(hud);
        canvas.line(100, 100, 300, 100).width(10).dash(10, 30).pick(1); // dashes at x 100-110, 140-150, ...
        canvas.circle(500, 300, 100).noFill().stroke(8, Color.WHITE).dash(20, 20).strokeAlign(StrokeAlign.OUTSIDE)
                .pick(2);
        pipeline.render();
        FrameInfo frame = pipeline.lastFrame();

        assertEquals(1, frame.pick(hud, 105, 100));
        assertEquals(1, frame.pick(hud, 145, 102));
        assertEquals(0, frame.pick(hud, 125, 100), "in a gap");
        assertEquals(2, frame.pick(hud, 500, 300), "inside: the shape's area");
        int dashes = 0, gaps = 0;
        for (int k = 0; k < 360; k++) { // around the outside stroke band: both dashes and gaps
            double a = Math.toRadians(k);
            int id = frame.pick(hud, 500 + (float) Math.cos(a) * 104, 300 + (float) Math.sin(a) * 104);
            if (id == 2) {
                dashes++;
            } else {
                gaps++;
            }
        }
        assertTrue(dashes > 120 && gaps > 120, dashes + " dash samples, " + gaps + " gap samples");
        pipeline.dispose();
    }

    @Test
    void nineSlicesArePickedInsideTheirRectangle() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        RenderPipeline pipeline = single(backend, hud);
        pipeline.canvas(hud).ninePatch(a, 100, 100, 200, 80).insets(2).pick(9);
        pipeline.render();
        assertEquals(9, pipeline.lastFrame().pick(hud, 101, 101));
        assertEquals(9, pipeline.lastFrame().pick(hud, 200, 140));
        assertEquals(0, pipeline.lastFrame().pick(hud, 301, 140));
        pipeline.dispose();
    }

    // endregion

    // region robustness

    @Test
    void aCustomPassRecordingIntoADrawnLayerDoesNotBreakTheNextFrames() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        Canvas[] late = new Canvas[1];
        RenderPipeline pipeline = RenderPipeline.builder(backend)
                .pass(SCREEN, hud)
                .custom(be -> {
                    late[0].rect(0, 0, 5, 5).pick(2); // too late for this frame's layer pass
                    late[0].rect(10, 0, 5, 5);
                })
                .pass(SCREEN, world, hud) // hud again: drawn with what the custom pass added
                .build();
        late[0] = pipeline.canvas(hud);
        pipeline.canvas(hud).rect(100, 100, 10, 10).pick(1);
        pipeline.render();
        FrameInfo frame = pipeline.lastFrame();
        assertEquals(1, frame.pick(hud, 105, 105));
        assertEquals(2, frame.pick(hud, 2, 2), "picked as drawn by the second pass");
        FrameInfo.LayerStats[] layers = frame.getLayers();
        assertEquals(1, layers[0].getCommands());
        assertEquals(3, layers[2].getCommands(), "the second pass counts what it drew");

        pipeline.render(); // the pipeline is not stuck
        assertEquals(2, frame.getFrameNumber());
        pipeline.dispose();
    }

    @Test
    void aFailingFrameLeavesThePipelineUsable() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        boolean[] fail = {true};
        RenderPipeline pipeline = RenderPipeline.builder(backend)
                .pass(SCREEN, hud)
                .custom(be -> {
                    if (fail[0]) {
                        throw new IllegalStateException("boom");
                    }
                })
                .build();
        pipeline.canvas(hud).rect(0, 0, 10, 10).pick(1);
        assertThrows(IllegalStateException.class, pipeline::render);
        fail[0] = false;
        pipeline.canvas(hud).rect(50, 0, 10, 10).pick(2);
        pipeline.render();
        assertEquals(0, pipeline.lastFrame().pick(hud, 5, 5));
        assertEquals(2, pipeline.lastFrame().pick(hud, 55, 5));
        pipeline.dispose();
    }

    @Test
    void aLayerInTwoPassesHasStatsPerPassAndCullingIsCounted() {
        RecordingBackend backend = new RecordingBackend(16, 16384, true);
        RenderPipeline pipeline = RenderPipeline.builder(backend)
                .pass(SCREEN, world)
                .pass(view(5000, 5000, 5100, 5100), world) // sees nothing
                .build();
        pipeline.canvas(world).rect(0, 0, 10, 10).pick(1);
        pipeline.canvas(world).rect(20, 0, 10, 10);
        pipeline.render();
        FrameInfo frame = pipeline.lastFrame();
        FrameInfo.LayerStats first = frame.getLayers()[0], second = frame.getLayers()[1];
        assertEquals(0, first.getPass());
        assertEquals(1, second.getPass());
        assertEquals(2, first.getCommands());
        assertEquals(0, first.getCulled());
        assertEquals(1, first.getBatches());
        assertEquals(2, second.getCommands());
        assertEquals(2, second.getCulled());
        assertEquals(0, second.getBatches());
        assertEquals(0, frame.getPasses()[1].getDrawCalls());
        assertEquals(1, frame.pick(world, 5, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> frame.getBatchBreak(1));
        Material named = Material.effect("vec4 effect(vec4 c, vec2 uv, Fragment f) { return c; }").setName("water");
        assertTrue(named.toString().startsWith("Material[water, EFFECT #"), named.toString());
        pipeline.dispose();
    }

    // endregion

    @Test
    void framesWithStatsAndPicksDoNotAllocate() {
        RecordingBackend backend = new RecordingBackend(16, 16384, false);
        RenderPipeline pipeline = RenderPipeline.builder(backend).pass(SCREEN, world).pass(SCREEN, hud).build();
        var font = TestFonts.mono();
        int[] ids = new int[4];
        Runnable frame = () -> {
            Canvas w = pipeline.canvas(world), h = pipeline.canvas(hud);
            for (int i = 0; i < 1000; i++) {
                w.roundRect(i % 800, i % 600, 20, 10, 3).stroke(1, Color.WHITE).depth(i % 7).pick(i + 1);
                w.image(i % 2 == 0 ? a : b, i % 800, 0).depth(i % 7).pick(-i - 1);
            }
            h.text(font, "Score", 10, 10).size(20).pick(5000);
            pipeline.render();
            FrameInfo info = pipeline.lastFrame();
            info.pick(world, 100, 100);
            info.pickAll(hud, 12, 15, ids);
            info.getBreaks(BatchBreak.TEXTURE_SLOTS);
            info.getDrawCalls();
        };
        for (int i = 0; i < 100; i++) {
            frame.run();
        }
        com.sun.management.ThreadMXBean threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long thread = Thread.currentThread().getId();
        long[] windows = new long[5];
        for (int w = 0; w < windows.length; w++) {
            long before = threads.getThreadAllocatedBytes(thread);
            for (int i = 0; i < 20; i++) {
                frame.run();
            }
            windows[w] = threads.getThreadAllocatedBytes(thread) - before;
        }
        assertEquals(0, Arrays.stream(windows).min().orElseThrow(),
                "bytes allocated per 20-frame window: " + Arrays.toString(windows));
        pipeline.dispose();
    }
}
