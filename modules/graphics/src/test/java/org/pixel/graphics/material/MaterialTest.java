package org.pixel.graphics.material;

import org.junit.jupiter.api.Test;
import org.pixel.commons.Color;
import org.pixel.content.Texture;
import org.pixel.graphics.backend.BackendCapabilities;
import org.pixel.graphics.backend.BatchList;
import org.pixel.graphics.backend.RenderData;
import org.pixel.graphics.canvas.Canvas;
import org.pixel.graphics.draw.Batcher;
import org.pixel.graphics.draw.DrawList;
import org.pixel.graphics.draw.Layer;
import org.pixel.graphics.draw.LayerMode;
import org.pixel.graphics.draw.ShapeType;
import org.pixel.graphics.draw.StyleTable;
import org.pixel.graphics.pass.RenderPipeline;
import org.pixel.graphics.pass.View;
import org.pixel.graphics.testing.RecordingBackend;
import org.pixel.graphics.testing.RecordingBackend.Batch;
import org.pixel.graphics.testing.TestTexture;
import org.pixel.math.Matrix4;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Materials: values and sources, compiling and hot reload, how commands carry them, how they batch and sort.
 */
class MaterialTest {

    private static final String FLASH = "vec4 effect(vec4 color, vec2 uv, Fragment f) {\n"
            + "    return vec4(mix(color.rgb, vec3(color.a), f.data.x), color.a);\n}\n";
    private static final View SCREEN = new View() {
        private final Matrix4 matrix = Matrix4.orthographic(0, 800, 600, 0, 0f, 1f);

        @Override
        public Matrix4 getViewMatrix() {
            return matrix;
        }

        @Override
        public float getPixelsPerUnit() {
            return 1f;
        }
    };

    private final RecordingBackend backend = new RecordingBackend(16, 16384, true);
    private final Texture a = new TestTexture(8, 8);
    private final Texture b = new TestTexture(8, 8);

    private static void shape(DrawList list, Texture texture, float depth) {
        list.addShape(texture != null ? ShapeType.TEXTURED : ShapeType.RECT, texture, 0, 0, 1, 0, 1, 1, 0, 1,
                0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0xFFFFFFFF, 0, 0, depth);
    }

    // region values and sources

    @Test
    void uniformsAndTexturesAreNamedValuesThatBumpTheSerial() {
        Material m = Material.effect(FLASH);
        int serial = m.getValuesSerial();
        m.set("uAmount", 0.5f).set("uOffset", 1f, 2f).set("uAmount", 0.75f).set("uTint", Color.RED);
        assertEquals(3, m.getUniformCount());
        assertEquals("uAmount", m.getUniformName(0));
        assertEquals(0.75f, m.getUniformValue(0, 0));
        assertEquals(2, m.getUniformSize(1));
        assertEquals(4, m.getUniformSize(2));
        assertTrue(m.getValuesSerial() > serial);

        m.texture("uNoise", a).texture("uRamp", b).texture("uNoise", b);
        assertEquals(2, m.getTextureCount());
        assertSame(b, m.getTexture(0));
        m.texture("uNoise", null);
        assertEquals(1, m.getTextureCount());
        assertEquals("uRamp", m.getTextureName(0));
        for (int i = 0; i < Material.MAX_TEXTURES - 1; i++) {
            m.texture("uExtra" + i, a);
        }
        assertThrows(IllegalStateException.class, () -> m.texture("uOneTooMany", a));
    }

    @Test
    void sourcesAreCheckedAndMatchTheirKind() {
        assertThrows(IllegalArgumentException.class, () -> Material.effect("void main() {}"));
        assertThrows(IllegalArgumentException.class, () -> Material.raw("void main() {}", null));
        Material effect = Material.effect(FLASH);
        Material raw = Material.raw("void main() {}", "void main() {}");
        assertThrows(IllegalStateException.class, () -> effect.setSource("a", "b"));
        assertThrows(IllegalStateException.class, () -> raw.setSource(FLASH));
        assertNotEquals(effect.getId(), raw.getId());
        assertTrue(effect.getId() > 0 && effect.getId() <= 0xFFFF);
    }

    // endregion

    // region compiling and hot reload

    @Test
    void compiledOnFirstUseAndAgainOnlyAfterASourceChange() {
        Material m = Material.effect(FLASH);
        var first = m.program(backend);
        assertSame(first, m.program(backend));
        assertEquals(1, backend.compiledMaterials.size());

        m.setSource(FLASH + "// v2");
        var second = m.program(backend);
        assertNotSame(first, second);
        assertTrue(backend.compiledMaterials.get(0).disposed, "the replaced program is released");
        m.dispose();
        assertTrue(backend.compiledMaterials.get(1).disposed);
        assertThrows(IllegalStateException.class, () -> m.program(backend));
    }

    @Test
    void aReloadThatFailsKeepsThePreviousProgramAndRetriesOnlyOnTheNextChange() {
        Material m = Material.effect(FLASH);
        var good = m.program(backend);
        backend.failOn = "typo";
        m.setSource(FLASH + "typo");
        assertSame(good, m.program(backend));
        assertTrue(m.getError().contains("typo"));
        assertSame(good, m.program(backend)); // not retried every frame
        assertFalse(backend.compiledMaterials.get(0).disposed);

        backend.failOn = null;
        m.setSource(FLASH);
        assertNotSame(good, m.program(backend));
        assertNull(m.getError());
    }

    @Test
    void aContextLostWhileAnEditIsBrokenRebuildsTheLastSourceThatCompiled() {
        Material m = Material.effect(FLASH);
        m.program(backend);
        backend.failOn = "typo";
        m.setSource(FLASH + "typo");
        m.program(backend); // fails: the first program keeps running
        backend.contextGeneration++;
        var rebuilt = m.program(backend); // no throw: the last good source is compiled for the new context
        assertEquals(FLASH, ((RecordingBackend.CompiledMaterial) rebuilt).source);
        assertTrue(m.getError().contains("typo"));
        assertSame(rebuilt, m.program(backend), "the broken edit is not retried every frame");

        backend.failOn = null;
        m.setSource(FLASH + "// fixed");
        assertTrue(((RecordingBackend.CompiledMaterial) m.program(backend)).source.endsWith("// fixed"));
        assertNull(m.getError());
    }

    @Test
    void aFirstCompileThatFailsThrows() {
        backend.failOn = "typo";
        Material m = Material.effect(FLASH + "typo");
        assertThrows(IllegalStateException.class, () -> m.program(backend));
    }

    @Test
    void aLostContextRecompilesWithoutReleasingDeadPrograms() {
        Material m = Material.effect(FLASH);
        m.program(backend);
        backend.contextGeneration++;
        m.program(backend);
        assertEquals(2, backend.compiledMaterials.size());
        assertFalse(backend.compiledMaterials.get(0).disposed, "it died with its context");
    }

    // endregion

    // region draw lists and batching

    @Test
    void materialDataIsMergedIntoTheCommandsStyle() {
        DrawList list = new DrawList(LayerMode.ORDERED);
        Material m = Material.effect(FLASH);
        float[] stroke = new float[StyleTable.FLOATS];
        stroke[StyleTable.STROKE_COLOR + 3] = 1f;
        stroke[StyleTable.STROKE_WIDTH] = 2f;
        int strokeId = list.styles().intern(stroke);

        list.setMaterial(m, 0.25f, 1f, 2f, 3f);
        shape(list, a, 0);
        list.addShape(ShapeType.RECT, null, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0xFFFFFFFF,
                strokeId, 0, 0);
        list.setMaterial(null);
        shape(list, a, 0);

        assertSame(m, list.material(0));
        assertSame(m, list.material(1));
        assertNull(list.material(2));
        assertEquals(0, list.styleId(2));
        int data = list.styleId(0), both = list.styleId(1);
        assertEquals(0.25f, list.styles().get(data, StyleTable.MATERIAL_DATA));
        assertEquals(3f, list.styles().get(data, StyleTable.MATERIAL_DATA + 3));
        assertEquals(0f, list.styles().get(data, StyleTable.STROKE_WIDTH)); // only the data
        assertEquals(2f, list.styles().get(both, StyleTable.STROKE_WIDTH)); // the stroke plus the data
        assertEquals(1f, list.styles().get(both, StyleTable.MATERIAL_DATA + 1));
        assertThrows(IllegalStateException.class, () -> {
            list.setMaterial(m);
            list.addQuad(a, 0, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0);
        });
    }

    @Test
    void aMaterialChangeBreaksTheBatchAndLimitsItsTextureSlots() {
        BackendCapabilities caps = new BackendCapabilities(6, 100, 8, 8, 8); // 6 slots: 2 left with a material
        DrawList list = new DrawList(LayerMode.ORDERED);
        Material m = Material.effect(FLASH);
        Texture[] textures = {a, b, new TestTexture(4, 4)};
        shape(list, a, 0);
        list.setMaterial(m, 1f, 0f, 0f, 0f);
        shape(list, a, 0);
        list.setMaterial(m, 2f, 0f, 0f, 0f); // other data: same batch
        shape(list, b, 0);
        shape(list, textures[2], 0); // a third texture doesn't fit the 2 material slots
        list.setMaterial(null);
        shape(list, a, 0);
        list.prepare();
        RenderData data = new RenderData(caps.maxTextureSlots());
        new Batcher().build(list, caps, data);
        BatchList batches = data.batches();

        assertEquals(4, batches.size());
        assertNull(batches.material(0));
        assertSame(m, batches.material(1));
        assertEquals(2, batches.quadCount(1));
        assertEquals(2, batches.slotLimit(1));
        assertSame(m, batches.material(2));
        assertEquals(6, batches.slotLimit(3));
    }

    @Test
    void batchByMaterialGroupsEqualDepthsByMaterialThenTexture() {
        DrawList list = new DrawList(LayerMode.SORTED_BY_MATERIAL);
        Material m = Material.effect(FLASH);
        for (int i = 0; i < 6; i++) { // interleaved: a, b, a+m, b, a, b+m ...
            list.setMaterial(i % 3 == 2 ? m : null);
            shape(list, i % 2 == 0 ? a : b, 1);
        }
        list.setMaterial(null);
        shape(list, b, 0); // lower depth: still first
        list.prepare();

        assertEquals(6, list.orderedIndex(0));
        Material previousMaterial = null;
        int groups = 0;
        Texture previousTexture = null;
        for (int order = 1; order < 7; order++) {
            int index = list.orderedIndex(order);
            if (order == 1 || list.material(index) != previousMaterial || list.texture(index) != previousTexture) {
                groups++;
            }
            previousMaterial = list.material(index);
            previousTexture = list.texture(index);
        }
        assertEquals(4, groups); // (none, a), (none, b), (m, a), (m, b) in some order
        int first = list.orderedIndex(1), second = list.orderedIndex(2);
        if (list.material(first) == list.material(second) && list.texture(first) == list.texture(second)) {
            assertTrue(first < second, "submission order within a group");
        }
        assertThrows(IllegalStateException.class, () -> Layer.ordered("hud").batchByMaterial());
        assertEquals(LayerMode.SORTED_BY_MATERIAL, Layer.sorted("tiles").batchByMaterial().getMode());
    }

    @Test
    void batchByMaterialStillSortsByDepthFirstIncludingNegativeDepths() {
        DrawList list = new DrawList(LayerMode.SORTED_BY_MATERIAL);
        Material m = Material.effect(FLASH);
        float[] depths = {3f, -2f, 0f, -7.5f, 3f, -2f, 1e6f, -1e6f};
        for (int i = 0; i < depths.length; i++) {
            list.setMaterial(i % 2 == 0 ? m : null);
            list.addShape(ShapeType.TEXTURED, i % 3 == 0 ? a : b, i, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 0, 0,
                    0, 0, 0, 0, 0xFFFFFFFF, 0, 0, depths[i]);
        }
        list.prepare();
        float previous = Float.NEGATIVE_INFINITY;
        for (int order = 0; order < depths.length; order++) {
            float depth = depths[(int) list.x(list.orderedIndex(order), 0)];
            assertTrue(depth >= previous, "depth order at " + order);
            previous = depth;
        }
    }

    // endregion

    // region canvas and pipeline

    @Test
    void aCanvasCommandsMaterialAppliesToThatCommandOnly() {
        Layer layer = Layer.ordered("test");
        RenderPipeline pipeline = RenderPipeline.builder(backend).pass(SCREEN, layer).build();
        Material m = Material.effect(FLASH);
        Canvas c = pipeline.canvas(layer);
        c.image(a, 0, 0).material(m, 0.5f);
        c.image(a, 10, 0).material(m, 0.75f);
        c.image(a, 20, 0);
        c.text(org.pixel.graphics.testing.TestFonts.mono(), "Hi", 0, 50).size(16).material(m);
        c.rect(0, 0, 4, 4);
        pipeline.render();

        List<Batch> batches = backend.lastPass().batches();
        assertEquals(4, batches.size());
        assertSame(m, batches.get(0).material());
        assertEquals(2, batches.get(0).quadCount());
        assertEquals(2, batches.get(0).styles().size()); // one record per data value
        assertEquals(0.75f, batches.get(0).styles().get(1)[StyleTable.MATERIAL_DATA]);
        assertNull(batches.get(1).material());
        assertSame(m, batches.get(2).material()); // every glyph quad of the text
        assertEquals(2, batches.get(2).quadCount());
        assertNull(batches.get(3).material());

        c.ninePatch(a, 100, 100, 60, 40).insets(2, 2, 2, 2).material(m);
        pipeline.render();
        var vertices = backend.lastPass().shapes();
        assertEquals(36, vertices.size()); // 9 parts
        for (var v : vertices) { // local coordinates from the center of the whole nine-patch
            assertEquals(v.x() - 130f, v.localX(), 1e-4f);
            assertEquals(v.y() - 120f, v.localY(), 1e-4f);
        }

        c.image(a, 0, 0).material(m); // started but dropped by the reset at the end of render()
        pipeline.canvas(layer).reset();
        c.image(a, 0, 0);
        pipeline.render();
        assertNull(backend.lastPass().batches().get(0).material(), "a reused builder starts without material");
        pipeline.dispose();
    }

    @Test
    void sourceChangesAreCompiledOnTheNextRender() {
        Layer layer = Layer.ordered("test");
        RenderPipeline pipeline = RenderPipeline.builder(backend).pass(SCREEN, layer).build();
        Material m = Material.effect(FLASH);
        pipeline.canvas(layer).image(a, 0, 0).material(m);
        pipeline.render();
        m.setSource(FLASH + "// edited");
        pipeline.canvas(layer).image(a, 0, 0).material(m);
        pipeline.render();
        assertEquals(2, backend.compiledMaterials.size());
        assertTrue(backend.compiledMaterials.get(1).source.endsWith("// edited"));
        pipeline.dispose();
    }

    @Test
    void batchByMaterialLayersNeedFewerDrawCalls() {
        Layer plain = Layer.sorted("plain"), grouped = Layer.sorted("grouped").batchByMaterial();
        RenderPipeline pipeline = RenderPipeline.builder(backend).pass(SCREEN, plain, grouped).build();
        Material water = Material.effect(FLASH);
        for (Layer layer : List.of(plain, grouped)) {
            Canvas c = pipeline.canvas(layer);
            for (int i = 0; i < 100; i++) { // a tile row: grass and animated water alternate
                var tile = c.image(a, i * 8, 0).depth(0);
                if (i % 2 == 1) {
                    tile.material(water, i);
                }
            }
        }
        pipeline.render();
        assertEquals(100, backend.passes.get(0).batches().size());
        assertEquals(2, backend.passes.get(1).batches().size());
        pipeline.dispose();
    }

    @Test
    void framesWithMaterialsDoNotAllocate() {
        Layer world = Layer.sorted("world").batchByMaterial();
        RecordingBackend counting = new RecordingBackend(16, 16384, false);
        RenderPipeline pipeline = RenderPipeline.builder(counting).pass(SCREEN, world).build();
        Material flash = Material.effect(FLASH).texture("uNoise", b);
        float[] time = {0f};
        var font = org.pixel.graphics.testing.TestFonts.mono();
        Runnable frame = () -> {
            Canvas c = pipeline.canvas(world);
            time[0] += 0.016f;
            flash.set("uTime", time[0]);
            for (int i = 0; i < 1000; i++) {
                c.image(i % 3 == 0 ? a : b, i % 800, i % 600).depth(i % 10).material(flash, (i % 8) * 0.125f);
                c.rect(i % 800, 0, 4, 4).stroke(1, Color.WHITE).depth(i % 10).material(flash, 0.5f);
            }
            c.text(font, "Score", 10, 10).size(20).material(flash, 1f);
            pipeline.render();
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

    // endregion
}
