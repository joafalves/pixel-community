package org.pixel.graphics.pass;

import org.junit.jupiter.api.Test;
import org.pixel.commons.Color;
import org.pixel.graphics.backend.RenderTarget;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.testing.RecordingBackend;
import org.pixel.graphics.testing.RecordingBackend.FullVertex;
import org.pixel.math.Matrix4;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RenderPipelineTest {

    private static final float EPS = 1e-4f;

    private final RecordingBackend backend = new RecordingBackend(16, 16384, true);
    private final Layer world = Layer.sorted("world");
    private final Layer fx = Layer.ordered("fx").blend(BlendMode.ADDITIVE);
    private final Layer hud = Layer.ordered("hud");

    /** A view showing layer coordinates [x0, x1] x [y0, y1] (y down). */
    private static View view(float x0, float y0, float x1, float y1, float pixelsPerUnit) {
        Matrix4 matrix = Matrix4.orthographic(x0, x1, y1, y0, 0f, 1f);
        return new View() {
            @Override
            public Matrix4 getViewMatrix() {
                return matrix;
            }

            @Override
            public float getPixelsPerUnit() {
                return pixelsPerUnit;
            }
        };
    }

    private static float minX(List<FullVertex> quad) {
        return Math.min(Math.min(quad.get(0).x(), quad.get(1).x()), Math.min(quad.get(2).x(), quad.get(3).x()));
    }

    @Test
    void passesRunInOrderAndLayersInTheirListedOrderWithTheirBlendModes() {
        View camera = view(0, 0, 1000, 1000, 1), screen = view(-500, 0, 500, 500, 1);
        RenderPipeline pipeline = RenderPipeline.builder(backend)
                .pass(camera, world, fx)
                .pass(screen, hud)
                .build();
        pipeline.canvas(hud).rect(1, 1, 10, 10).fill(Color.WHITE);  // recording order doesn't matter
        pipeline.canvas(fx).circle(50, 50, 5).fill(Color.RED);
        pipeline.canvas(world).rect(20, 20, 10, 10).fill(Color.BLUE);
        pipeline.render();

        assertEquals(3, backend.passes.size());
        assertEquals(BlendMode.NORMAL_BLEND, backend.passes.get(0).blendMode()); // world
        assertEquals(BlendMode.ADDITIVE, backend.passes.get(1).blendMode());     // fx
        assertEquals(BlendMode.NORMAL_BLEND, backend.passes.get(2).blendMode()); // hud
        assertEquals(camera.getViewMatrix().toUnsafeArray()[3][0], backend.passes.get(0).viewTranslationX(), EPS);
        assertEquals(screen.getViewMatrix().toUnsafeArray()[3][0], backend.passes.get(2).viewTranslationX(), EPS);
        assertNotEquals(backend.passes.get(0).viewTranslationX(), backend.passes.get(2).viewTranslationX());
    }

    @Test
    void emptyAndFullyCulledLayersSubmitNothing() {
        RenderPipeline pipeline = RenderPipeline.builder(backend).pass(view(0, 0, 100, 100, 1), world, hud).build();
        pipeline.canvas(hud).rect(500, 500, 10, 10).fill(Color.WHITE); // off screen
        pipeline.render();
        assertEquals(0, backend.passes.size());
    }

    @Test
    void commandsOutsideTheViewAreCulledButEdgesAndMarginsKept() {
        RenderPipeline pipeline = RenderPipeline.builder(backend).pass(view(0, 0, 100, 100, 1), world).build();
        Canvas c = pipeline.canvas(world);
        c.rect(10, 10, 10, 10).fill(Color.WHITE);    // inside
        c.rect(95, 40, 30, 10).fill(Color.WHITE);    // straddles the right edge
        c.rect(101, 40, 10, 10).fill(Color.WHITE);   // its anti-aliasing margin still reaches the view
        c.rect(300, 300, 10, 10).fill(Color.WHITE);  // far outside
        c.circle(-50, 50, 10).fill(Color.WHITE);     // far left
        pipeline.render();

        assertEquals(3 * 4, backend.lastPass().shapes().size());
    }

    @Test
    void aLayerDrawnByTwoPassesIsRecordedOnceAndCulledPerView() {
        View main = view(0, 0, 100, 100, 1), minimap = view(0, 0, 1000, 1000, 0.1f);
        RenderPipeline pipeline = RenderPipeline.builder(backend).pass(main, world).pass(minimap, world).build();
        Canvas c = pipeline.canvas(world);
        c.rect(500, 500, 10, 10).fill(Color.RED).depth(1);  // only in the minimap
        c.rect(10, 10, 10, 10).fill(Color.WHITE).depth(5);
        c.rect(30, 30, 10, 10).fill(Color.BLUE).depth(0);
        pipeline.render();

        assertEquals(2, backend.passes.size());
        assertEquals(2 * 4, backend.passes.get(0).shapes().size());
        List<FullVertex> all = backend.passes.get(1).shapes();
        assertEquals(3 * 4, all.size());
        // sorted by depth in both passes: blue (0), red (1), white (5); margins are 1 px in the coarsest view
        // (the minimap, 0.1 px per unit), so 10 units
        assertEquals(30, minX(all.subList(0, 4)) + 10, EPS);
        assertEquals(500, minX(all.subList(4, 8)) + 10, EPS);
        assertEquals(10, minX(all.subList(8, 12)) + 10, EPS);
    }

    @Test
    void antiAliasingMarginsFollowTheViewScale() {
        RenderPipeline sharp = RenderPipeline.builder(backend).pass(view(0, 0, 100, 100, 4f), hud).build();
        sharp.canvas(hud).rect(10, 10, 10, 10).fill(Color.WHITE);
        sharp.render();
        assertEquals(10 - 0.25f, minX(backend.lastPass().shapes()), EPS); // 1 pixel at 4 pixels per unit

        RenderPipeline zoomedOut = RenderPipeline.builder(backend).pass(view(0, 0, 1000, 1000, 0.25f), hud).build();
        zoomedOut.canvas(hud).rect(10, 10, 10, 10).fill(Color.WHITE);
        zoomedOut.render();
        assertEquals(10 - 4f, minX(backend.lastPass().shapes()), EPS); // 1 pixel at 0.25 pixels per unit
    }

    @Test
    void aLayerInTwoPassesUsesTheCoarsestViewForMarginsAndTheFinestForFringes() {
        View main = view(0, 0, 100, 100, 1f), minimap = view(0, 0, 1000, 1000, 0.1f);
        for (boolean minimapFirst : new boolean[]{false, true}) {
            backend.passes.clear();
            RenderPipeline pipeline = minimapFirst
                    ? RenderPipeline.builder(backend).pass(minimap, world).pass(main, world).build()
                    : RenderPipeline.builder(backend).pass(main, world).pass(minimap, world).build();
            Canvas c = pipeline.canvas(world);
            c.rect(10, 10, 10, 10).fill(Color.WHITE);
            c.path().moveTo(40, 40).lineTo(60, 40).lineTo(50, 60).close().fill(Color.WHITE);
            pipeline.render();

            List<FullVertex> shapes = backend.passes.get(0).shapes();
            assertEquals(10 - 10f, minX(shapes.subList(0, 4)), EPS, "margin: 1 px in the minimap (0.1 px/unit)");
            float fringeLeft = Float.MAX_VALUE;
            for (FullVertex v : shapes.subList(4, shapes.size())) {
                fringeLeft = Math.min(fringeLeft, v.x());
            }
            assertTrue(fringeLeft > 40 - 1.5f, "path fringe: about 1 px in the main view, not 10 units");
        }
    }

    @Test
    void layersAreClearedAndCanvasesResetAfterEveryFrame() {
        RenderPipeline pipeline = RenderPipeline.builder(backend).pass(view(0, 0, 1000, 1000, 1), hud).build();
        Canvas c = pipeline.canvas(hud);
        c.translate(100, 0);
        c.clip(0, 0, 50, 50);
        c.rect(10, 10, 10, 10).fill(Color.WHITE);
        pipeline.render();

        assertSame(c, pipeline.canvas(hud));
        c.rect(10, 10, 10, 10).fill(Color.WHITE);
        pipeline.render();
        assertEquals(4, backend.lastPass().shapes().size()); // only this frame's command
        assertEquals(10 - 1f, minX(backend.lastPass().shapes()), EPS); // no translation left
        assertEquals(0, backend.lastPass().shapes().get(0).clip());   // no clip left
    }

    @Test
    void customPassesRunAtTheirPositionWithBackendAccess() {
        List<String> events = new ArrayList<>();
        RenderPipeline pipeline = RenderPipeline.builder(backend)
                .custom(b -> events.add("before:" + b.capabilities().maxTextureSlots()))
                .pass(view(0, 0, 100, 100, 1), hud)
                .custom(b -> events.add("after:" + backend.passes.size()))
                .build();
        pipeline.canvas(hud).rect(1, 1, 5, 5).fill(Color.WHITE);
        pipeline.render();
        assertEquals(List.of("before:16", "after:1"), events);
    }

    @Test
    void misuseIsReported() {
        RenderPipeline pipeline = RenderPipeline.builder(backend).pass(view(0, 0, 1, 1, 1), hud).build();
        assertThrows(IllegalArgumentException.class, () -> pipeline.canvas(world));
        assertThrows(IllegalStateException.class, () -> RenderPipeline.builder(backend).build());
        assertThrows(IllegalArgumentException.class,
                () -> RenderPipeline.builder(backend).pass(view(0, 0, 1, 1, 1), hud, hud));
        assertThrows(IllegalArgumentException.class, () -> RenderPipeline.builder(backend).pass(null, hud));
        RenderPipeline nested = RenderPipeline.builder(backend).custom(b -> pipeline.render()).build();
        assertDoesNotThrow(nested::render); // another pipeline may render from a custom pass
        RenderPipeline self = RenderPipeline.builder(backend).custom(b -> {
        }).build();
        RenderPipeline[] holder = {null};
        holder[0] = RenderPipeline.builder(backend).custom(b -> holder[0].render()).build();
        assertThrows(IllegalStateException.class, holder[0]::render);
        self.dispose();
        assertThrows(IllegalStateException.class, self::render);
        assertThrows(IllegalStateException.class, () -> self.canvas(hud));

        RenderPipeline[] recording = {null};
        recording[0] = RenderPipeline.builder(backend).custom(b -> recording[0].canvas(hud))
                .pass(view(0, 0, 1, 1, 1), hud).build();
        assertThrows(IllegalStateException.class, recording[0]::render); // no recording while rendering
    }

    @Test
    void steadyStateFramesDoNotAllocate() {
        RecordingBackend counting = new RecordingBackend(16, 16384, false);
        View camera = view(0, 0, 800, 600, 2f), screen = view(0, 0, 800, 600, 2f);
        RenderPipeline pipeline = RenderPipeline.builder(counting)
                .pass(camera, world, fx)
                .pass(view(0, 0, 4000, 3000, 0.2f), world) // minimap
                .pass(screen, hud)
                .build();
        Runnable frame = () -> {
            Canvas w = pipeline.canvas(world), f = pipeline.canvas(fx), h = pipeline.canvas(hud);
            for (int i = 0; i < 3000; i++) {
                w.rect(i % 3000, i % 2000, 16, 16).fill(Color.WHITE).depth(i % 50);
            }
            for (int i = 0; i < 300; i++) {
                f.circle(i, i, 4).fill(Color.RED);
            }
            h.roundRect(10, 10, 200, 40, 8).fill(Color.BLUE).stroke(2, Color.WHITE);
            pipeline.render();
        };
        assertSteadyStateDoesNotAllocate(frame);
    }

    // region targets and post effects

    /** A post effect that records itself in the backend's events and its disposal. */
    private static final class Probe extends ShaderEffect {
        boolean disposed;

        Probe() {
            super("vec4 effect(vec2 uv) { return texture(uSource, uv); }");
        }

        @Override
        public void dispose() {
            disposed = true;
            super.dispose();
        }
    }

    @Test
    void layersIntoATargetThenPostToTheScreenThenAHud() {
        RenderPipeline.Builder builder = RenderPipeline.builder(backend);
        RenderTarget scene = builder.screenTarget(1f);
        RenderPipeline pipeline = builder
                .pass(view(0, 0, 800, 600, 1), scene, world, fx)
                .post(scene, new Probe())
                .pass(view(-800, 0, 800, 600, 1), hud)
                .build();
        pipeline.canvas(world).rect(10, 10, 10, 10).fill(Color.WHITE);
        pipeline.canvas(fx).rect(20, 20, 10, 10).fill(Color.WHITE);
        pipeline.canvas(hud).rect(30, 30, 10, 10).fill(Color.WHITE);
        pipeline.render();

        assertEquals(800, scene.getWidth());
        assertEquals(600, scene.getHeight());
        assertEquals(List.of("clear t0", "post t0->screen blend"), backend.events); // cleared once, not per layer
        assertEquals(3, backend.passes.size());
        assertSame(scene, backend.passes.get(0).target());
        assertSame(scene, backend.passes.get(1).target());
        assertNull(backend.passes.get(2).target());
    }

    @Test
    void targetsAreClearedEveryFrameEvenWithNothingToDraw() {
        RenderPipeline.Builder builder = RenderPipeline.builder(backend);
        RenderTarget minimap = builder.target(128, 128);
        RenderPipeline pipeline = builder.pass(view(0, 0, 100, 100, 1), minimap, world).build();
        pipeline.render();
        pipeline.render();
        assertEquals(List.of("clear t0", "clear t0"), backend.events);
        assertTrue(backend.passes.isEmpty());
    }

    @Test
    void effectChainsPingPongAndOnlyTheLastStepBlendsOverTheScreen() {
        RenderPipeline.Builder builder = RenderPipeline.builder(backend);
        RenderTarget scene = builder.screenTarget(0.5f);
        RenderTarget output = backend.createTarget(64, 64); // the caller's
        RenderPipeline pipeline = builder
                .pass(view(0, 0, 800, 600, 1), scene, world)
                .post(scene, new Probe(), new Probe(), new Probe())
                .post(scene, output, new Probe(), new Probe())
                .post(scene)
                .build();
        pipeline.render();

        assertEquals(400, scene.getWidth()); // half the screen
        List<String> posts = backend.events.stream().filter(e -> e.startsWith("post")).toList();
        String a = backend.targets.get(2).name, b = backend.targets.get(3).name; // first pass's scratch targets
        String c = backend.targets.get(4).name;                                  // second pass's own
        assertEquals(List.of(
                "post t0->" + a, "post " + a + "->" + b, "post " + b + "->screen blend",
                "post t0->" + c, "post " + c + "->t1",       // into a target: replaced, not blended
                "post t0->screen blend"), posts);            // no effects: a plain copy
    }

    @Test
    void screenTargetsStartAtTheScreenSizeAndScratchTargetsStayStable() {
        RenderPipeline.Builder builder = RenderPipeline.builder(backend);
        RenderTarget scene = builder.screenTarget(1f);
        assertEquals(800, scene.getWidth()); // canvases record before the first render(): no 1x1 placeholder
        RenderTarget minimap = builder.target(160, 120);
        RenderPipeline pipeline = builder.pass(view(0, 0, 1, 1, 1), scene, world)
                .pass(view(0, 0, 1, 1, 1), minimap, world)
                .post(scene, Effects.bloom(0.5f, 1f), Effects.sepia(1f))
                .post(minimap, backend.createTarget(160, 120), Effects.bloom(0.5f, 1f), Effects.vignette(1f))
                .build();
        pipeline.render();
        int created = backend.targets.size();
        for (int i = 0; i < 5; i++) {
            pipeline.render();
        }
        assertEquals(created, backend.targets.size()); // no target re-created between frames
    }

    @Test
    void effectsRecompileAfterTheContextWasRecreated() {
        RenderPipeline.Builder builder = RenderPipeline.builder(backend);
        RenderTarget scene = builder.screenTarget(1f);
        RenderPipeline pipeline = builder.pass(view(0, 0, 1, 1, 1), scene, world).post(scene, new Probe()).build();
        pipeline.render();
        pipeline.render();
        assertEquals(1, backend.compiledPrograms);
        backend.contextGeneration++; // e.g. Android recreated the GL context
        pipeline.render();
        assertEquals(2, backend.compiledPrograms);
    }

    @Test
    void screenTargetsFollowTheScreenSize() {
        RenderPipeline.Builder builder = RenderPipeline.builder(backend);
        RenderTarget scene = builder.screenTarget(1f);
        RenderPipeline pipeline = builder.pass(view(0, 0, 1, 1, 1), scene, world).build();
        pipeline.render();
        backend.screenWidth = 1024;
        backend.screenHeight = 768;
        pipeline.render();
        assertEquals(1024, scene.getWidth());
        assertEquals(768, scene.getHeight());
    }

    @Test
    void passesIntoTargetsMeasurePixelsAgainstTheTarget() {
        RenderPipeline.Builder builder = RenderPipeline.builder(backend);
        RenderTarget minimap = builder.target(100, 100);
        RenderPipeline pipeline = builder.pass(view(0, 0, 1000, 1000, 1f), minimap, hud).build();
        pipeline.canvas(hud).rect(500, 500, 50, 50).fill(Color.WHITE);
        pipeline.render();
        // 1000 units on 100 pixels: 0.1 px per unit, so the 1 px margin is 10 units (the view's own 1 is ignored)
        assertEquals(500 - 10f, minX(backend.lastPass().shapes()), EPS);
    }

    @Test
    void bloomRunsItsPassesOnHalfResolutionScratchTargets() {
        RenderPipeline.Builder builder = RenderPipeline.builder(backend);
        RenderTarget scene = builder.screenTarget(1f);
        RenderPipeline pipeline = builder.pass(view(0, 0, 1, 1, 1), scene, world)
                .post(scene, Effects.bloom(0.7f, 1f)).build();
        pipeline.render();

        List<String> posts = backend.events.stream().filter(e -> e.startsWith("post")).toList();
        assertEquals(6, posts.size()); // bright, 2 x (horizontal, vertical), combine
        assertTrue(posts.get(0).startsWith("post t0->"));
        assertEquals("post t0->screen blend", posts.get(5));
        RecordingBackend.Target half = backend.targets.get(1); // the first scratch target
        assertEquals(400, half.getWidth());
        assertEquals(300, half.getHeight());
    }

    @Test
    void disposingReleasesOwnedTargetsAndEffectsButNotTheCallersTargets() {
        RenderPipeline.Builder builder = RenderPipeline.builder(backend);
        RenderTarget owned = builder.screenTarget(1f);
        RenderTarget callers = backend.createTarget(32, 32);
        Probe effect = new Probe();
        RenderPipeline pipeline = builder.pass(view(0, 0, 1, 1, 1), owned, world)
                .post(owned, callers, effect).build();
        pipeline.render();
        pipeline.dispose();

        assertTrue(((RecordingBackend.Target) owned).disposed);
        assertFalse(((RecordingBackend.Target) callers).disposed);
        assertTrue(effect.disposed);
        assertThrows(IllegalArgumentException.class,
                () -> RenderPipeline.builder(backend).post(callers, callers));
    }

    @Test
    void framesWithTargetsAndEffectsDoNotAllocate() {
        RecordingBackend counting = new RecordingBackend(16, 16384, false);
        RenderPipeline.Builder builder = RenderPipeline.builder(counting);
        RenderTarget scene = builder.screenTarget(1f);
        RenderTarget minimap = builder.target(128, 128);
        Effects.Vignette vignette = Effects.vignette(0.5f);
        RenderPipeline pipeline = builder
                .pass(view(0, 0, 800, 600, 1), scene, world, fx)
                .pass(view(0, 0, 4000, 3000, 1), minimap, world)
                .post(scene, Effects.bloom(0.7f, 1f), vignette, Effects.sepia(0.3f))
                .pass(view(0, 0, 800, 600, 1), hud)
                .build();
        Runnable frame = () -> {
            Canvas w = pipeline.canvas(world), h = pipeline.canvas(hud);
            for (int i = 0; i < 2000; i++) {
                w.rect(i % 800, i % 600, 8, 8).fill(Color.WHITE).depth(i % 30);
            }
            h.image(minimap.getTexture(), 10, 10);
            vignette.setStrength(0.5f);
            pipeline.render();
        };
        assertSteadyStateDoesNotAllocate(frame);
    }

    // endregion

    /**
     * Warm up, then measure five windows of 20 frames: at least one must allocate nothing. A real per-frame
     * allocation shows up in every window; the JIT occasionally allocates once while compiling late.
     */
    private static void assertSteadyStateDoesNotAllocate(Runnable frame) {
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
        assertEquals(0, java.util.Arrays.stream(windows).min().orElseThrow(),
                "bytes allocated per 20-frame window: " + java.util.Arrays.toString(windows));
    }
}
