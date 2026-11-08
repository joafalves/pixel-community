package org.pixel.graphics.draw;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SortKeysTest {

    @Test
    void orderedDepthPreservesFloatOrder() {
        float[] ascending = {Float.NEGATIVE_INFINITY, -1e9f, -100f, -1f, -0.5f, -Float.MIN_VALUE, 0f, Float.MIN_VALUE,
                0.5f, 1f, 2f, 100f, 1e9f, Float.POSITIVE_INFINITY};
        for (int i = 1; i < ascending.length; i++) {
            assertTrue(SortKeys.orderedDepth(ascending[i - 1]) < SortKeys.orderedDepth(ascending[i]),
                    ascending[i - 1] + " < " + ascending[i]);
        }
    }

    @Test
    void negativeZeroAndNaNAreZero() {
        assertEquals(SortKeys.orderedDepth(0f), SortKeys.orderedDepth(-0f));
        assertEquals(SortKeys.orderedDepth(0f), SortKeys.orderedDepth(Float.NaN));
    }

    @Test
    void keyOrdersByDepthThenSequence() {
        assertTrue(SortKeys.orderedDepth(-1f) < SortKeys.orderedDepth(0f));
        assertTrue(SortKeys.orderedDepth(3f) < SortKeys.orderedDepth(3.0001f));
    }
}
