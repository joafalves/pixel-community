/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas.text;

import java.nio.ByteBuffer;

/**
 * Platform-agnostic interface for generating SDF fonts from TrueType data.
 * Platform-specific implementations (e.g., GlSdfFontGenerator) handle the actual generation.
 */
public interface FontGenerator {

    /**
     * Generate an SDF font from TrueType font data.
     *
     * @param ttfData  The TrueType font file data
     * @param fontSize The font size to generate
     * @return The generated SDF font
     */
    SdfFont generate(ByteBuffer ttfData, int fontSize);

    /**
     * Generate an SDF font from TrueType font data with custom character set.
     *
     * @param ttfData  The TrueType font file data
     * @param fontSize The font size to generate
     * @param charset  The characters to include in the font
     * @return The generated SDF font
     */
    SdfFont generate(ByteBuffer ttfData, int fontSize, String charset);
}
