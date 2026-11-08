package org.pixel.graphics.draw;

import org.pixel.commons.Color;

/**
 * Packing of colors into the 4 x u8 RGBA format used by vertex data.
 * <p>
 * The packed int holds R in the lowest byte and A in the highest, so writing it with a little-endian int store puts
 * the bytes in R, G, B, A memory order (what the vertex layout expects).
 */
public final class Colors {

    private Colors() {
    }

    /**
     * Pack a color; channels are clamped to [0, 1].
     *
     * @param color The color.
     * @return The packed RGBA8 value.
     */
    public static int pack(Color color) {
        return pack(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    /**
     * Pack color channels; each is clamped to [0, 1].
     *
     * @param r Red.
     * @param g Green.
     * @param b Blue.
     * @param a Alpha.
     * @return The packed RGBA8 value.
     */
    public static int pack(float r, float g, float b, float a) {
        return toByte(r) | (toByte(g) << 8) | (toByte(b) << 16) | (toByte(a) << 24);
    }

    private static int toByte(float value) {
        if (!(value > 0f)) { // also catches NaN
            return 0;
        }
        if (value >= 1f) {
            return 255;
        }
        return (int) (value * 255f + 0.5f);
    }
}
