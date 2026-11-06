package org.pixel.graphics.draw;

import org.junit.jupiter.api.Test;
import org.pixel.content.Texture;
import org.pixel.graphics.testing.TestTexture;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DrawListTest {

    private static final Texture TEX = new TestTexture(8, 8);

    private static void add(DrawList list, float depth, float marker) {
        list.addQuad(TEX, marker, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, depth);
    }

    @Test
    void orderedModeKeepsSubmissionOrder() {
        DrawList list = new DrawList(LayerMode.ORDERED);
        add(list, 5, 0);
        add(list, -5, 1);
        add(list, 0, 2);
        list.prepare();
        for (int i = 0; i < 3; i++) {
            assertEquals(i, list.x(list.orderedIndex(i), 0));
        }
    }

    @Test
    void sortedModeIsAStableDepthSort() {
        DrawList list = new DrawList(LayerMode.SORTED);
        add(list, 2, 0);
        add(list, 1, 1);
        add(list, 2, 2);
        add(list, -3, 3);
        add(list, 1, 4);
        list.prepare();
        float[] expected = {3, 1, 4, 0, 2};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], list.x(list.orderedIndex(i), 0), "position " + i);
        }
    }

    @Test
    void sortIsGlobalAcrossManyCommands() {
        // the classic sprite batch only sorted within 256-sprite chunks; the draw list sorts everything
        DrawList list = new DrawList(LayerMode.SORTED);
        Random random = new Random(1);
        int n = 50_000;
        float[] depths = new float[n];
        for (int i = 0; i < n; i++) {
            depths[i] = random.nextInt(20) - 10 + (random.nextBoolean() ? random.nextFloat() : 0f);
            add(list, depths[i], i);
        }
        list.prepare();
        for (int i = 1; i < n; i++) {
            int a = list.orderedIndex(i - 1);
            int b = list.orderedIndex(i);
            assertTrue(depths[a] < depths[b] || (depths[a] == depths[b] && a < b), "order broken at " + i);
        }
    }

    @Test
    void allEqualDepthsKeepSubmissionOrderWithoutSorting() {
        DrawList list = new DrawList(LayerMode.SORTED);
        for (int i = 0; i < 1000; i++) {
            add(list, 7, i);
        }
        list.prepare();
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, list.orderedIndex(i));
        }
    }

    @Test
    void clearKeepsCapacityAndDropsTextures() {
        DrawList list = new DrawList(LayerMode.SORTED);
        for (int i = 0; i < 5000; i++) {
            add(list, i, i);
        }
        list.clear();
        assertEquals(0, list.size());
        add(list, 0, 0);
        list.prepare();
        assertEquals(1, list.size());
        assertSame(TEX, list.texture(0));
    }

    @Test
    void readingOrderBeforePrepareFails() {
        DrawList list = new DrawList(LayerMode.SORTED);
        add(list, 0, 0);
        assertThrows(IllegalStateException.class, () -> list.orderedIndex(0));
    }
}
