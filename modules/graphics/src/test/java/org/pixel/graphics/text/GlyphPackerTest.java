package org.pixel.graphics.text;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GlyphPackerTest {

    @Test
    void packsWithoutOverlapInsideAPowerOfTwoAtlas() {
        GlyphPacker packer = new GlyphPacker(1);
        Random random = new Random(7);
        int n = 300;
        int[] w = new int[n], h = new int[n];
        for (int i = 0; i < n; i++) {
            w[i] = 4 + random.nextInt(40);
            h[i] = 4 + random.nextInt(50);
            assertEquals(i, packer.add(w[i], h[i]));
        }
        packer.pack(4096);

        int aw = packer.getAtlasWidth(), ah = packer.getAtlasHeight();
        assertEquals(0, aw & (aw - 1));
        assertEquals(0, ah & (ah - 1));
        for (int i = 0; i < n; i++) {
            assertTrue(packer.x(i) >= 1 && packer.y(i) >= 1);
            assertTrue(packer.x(i) + w[i] + 1 <= aw && packer.y(i) + h[i] + 1 <= ah);
            for (int j = i + 1; j < n; j++) { // 1 px apart
                boolean apart = packer.x(i) + w[i] + 1 <= packer.x(j) || packer.x(j) + w[j] + 1 <= packer.x(i)
                        || packer.y(i) + h[i] + 1 <= packer.y(j) || packer.y(j) + h[j] + 1 <= packer.y(i);
                assertTrue(apart, "rectangles " + i + " and " + j + " overlap");
            }
        }
    }

    @Test
    void failsWhenTheMaximumSizeIsTooSmall() {
        GlyphPacker packer = new GlyphPacker(0);
        for (int i = 0; i < 5; i++) {
            packer.add(32, 32);
        }
        assertThrows(IllegalStateException.class, () -> packer.pack(64));
    }
}
