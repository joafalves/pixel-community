package org.pixel.graphics.draw;

/**
 * Helpers to build 64-bit sort keys whose signed {@code long} order matches the intended draw order.
 */
public final class SortKeys {

    private SortKeys() {
    }

    /**
     * Map a float depth to an int whose signed order equals the float order (so depth can live in the high bits of a
     * {@code long} key). {@code -0.0} and {@code 0.0} map to the same value; NaN is treated as 0.
     *
     * @param depth The depth.
     * @return The order-preserving int.
     */
    public static int orderedDepth(float depth) {
        if (depth != depth) { // NaN
            depth = 0f;
        }
        int bits = Float.floatToIntBits(depth + 0.0f); // + 0.0f folds -0.0 into 0.0
        return bits ^ ((bits >> 31) & 0x7FFFFFFF);
    }
}
