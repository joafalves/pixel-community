/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

/**
 * Constants used across the SDF (Signed Distance Field) rendering system.
 * These values must be kept in sync with font generation and shader parameters.
 */
public final class GlSdfConstants {

    /**
     * The SDF padding used by the font generator (in pixels).
     * This must match the padding value used when generating SDF fonts with STB TrueType.
     * The padding creates a distance field border around each glyph for smooth rendering.
     */
    public static final float SDF_PADDING_PX = 4.0f;

    /**
     * Default space character width as a ratio of font size.
     * Since space characters have no visual glyph, we use 25% of the font size as a standard width.
     */
    public static final float SPACE_WIDTH_RATIO = 0.25f;

    /**
     * SDF edge threshold value for text rendering.
     * This controls the "weight" or boldness of rendered text.
     * 
     * The font generator uses an on-edge value of 150 (out of 0-255), which normalizes to 150/255 ≈ 0.588.
     * This is the "true edge" of the glyph in the SDF texture.
     * 
     * - 0.588 = standard weight (at the exact edge defined during font generation)
     * - Higher values (e.g., 0.52, 0.55) = lighter/thinner text (threshold moves inside the glyph)
     * - Lower values (e.g., 0.50, 0.45) = bolder/thicker text (threshold moves outside the glyph)
     * 
     * Recommended range: 0.50 to 0.60
     */
    public static final float SDF_TEXT_EDGE_THRESHOLD = 0.54f;

    // Private constructor to prevent instantiation
    private GlSdfConstants() {
        throw new AssertionError("GlSdfConstants is a utility class and should not be instantiated");
    }
}
