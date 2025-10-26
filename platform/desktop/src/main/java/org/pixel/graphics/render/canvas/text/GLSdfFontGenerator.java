/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas.text;

import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;
import org.pixel.content.Texture;
import org.pixel.content.opengl.GLTexture;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;

/**
 * OpenGL implementation of FontGenerator using STB TrueType.
 * Platform-specific implementation using LWJGL.
 * 
 * <p>Note: This generates basic bitmap fonts. For production use, consider:
 * <ul>
 *   <li>Pre-baking fonts with msdfgen for better quality</li>
 *   <li>Implementing true SDF generation (distance field calculation)</li>
 * </ul>
 */
public class GLSdfFontGenerator implements SdfFontGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(GLSdfFontGenerator.class);

    // Character ranges for efficient multi-range font generation
    // Each range is [start, end) - end is exclusive
    private static final int[][] DEFAULT_CHAR_RANGES = {
        {32, 127},      // Basic ASCII (printable characters)
        {160, 256},     // Latin-1 Supplement (accented characters, currency, etc.)
    };

    // Common symbols useful for game development
    // Organized by category for clarity
    private static final int[] COMMON_SYMBOLS = {
        // General Punctuation (U+2000 - U+206F)
        8211,           // – En Dash
        8212,           // — Em Dash
        8216, 8217,     // ' ' Single quotes (curly)
        8220, 8221,     // " " Double quotes (curly)
        8226,           // • Bullet
        8230,           // … Ellipsis

        // Arrows (U+2190 - U+21FF)
        8592, 8593, 8594, 8595,  // ← ↑ → ↓ Basic arrows
        8596,           // ↔ Left-right arrow
        8656, 8657, 8658, 8659,  // ⇐ ⇑ ⇒ ⇓ Double arrows

        // Mathematical Operators (U+2200 - U+22FF)
        8704,           // ∀ For all
        8707,           // ∃ There exists
        8709,           // ∅ Empty set
        8721,           // ∑ Summation
        8730,           // √ Square root
        8734,           // ∞ Infinity
        8776,           // ≈ Almost equal
        8800,           // ≠ Not equal
        8804, 8805,     // ≤ ≥ Less/greater or equal

        // Miscellaneous Symbols (U+2600 - U+26FF)
        9733, 9734,     // ★ ☆ Stars (filled/empty)
        9728,           // ☀ Sun
        9729,           // ☁ Cloud
        9742,           // ☎ Telephone
        9744,           // ☐ Ballot box (checkbox)
        9745,           // ☑ Ballot box with check
        9746,           // ☒ Ballot box with X
        // Card/suit symbols
        9824,           // ♠ Spade (outline)
        9825,           // ♡ Heart (outline)
        9826,           // ♢ Diamond (outline)
        9827,           // ♣ Club (outline)
        9829,           // ♥ Heart (filled) - MAIN HEART
        9830,           // ♦ Diamond (filled)
        9834, 9835,     // ♪ ♫ Music notes

        // Geometric Shapes (U+25A0 - U+25FF)
        9632, 9633,     // ■ □ Squares (filled/empty)
        9642, 9643,     // ▪ ▫ Small squares
        9650, 9651,     // ▲ △ Triangles up
        9660, 9661,     // ▼ ▽ Triangles down
        9654, 9655,     // ► ▻ Triangles right
        9664, 9665,     // ◄ ◅ Triangles left
        9670,           // ◆ Diamond
        9679, 9675,     // ● ○ Circles (filled/empty)
        9702,           // ◦ White bullet

        // Box Drawing (U+2500 - U+257F) - useful for UI frames
        9472, 9474,     // ─ │ Horizontal/vertical lines
        9484, 9488, 9492, 9496,  // ┌ ┐ └ ┘ Box corners
        9500, 9508, 9516, 9524,  // ├ ┤ ┬ ┴ Box joints
    };

    @Override
    public SdfFont generate(ByteBuffer ttfData, int fontSize) {
        return generateWithRanges(ttfData, fontSize, DEFAULT_CHAR_RANGES, COMMON_SYMBOLS);
    }

    /**
     * Generate SDF font with custom character range (for compatibility with string-based API).
     * @deprecated Use generateWithRanges for better control
     */
    public SdfFont generate(ByteBuffer ttfData, int fontSize, String charset) {
        // Use default ranges + symbols
        return generateWithRanges(ttfData, fontSize, DEFAULT_CHAR_RANGES, COMMON_SYMBOLS);
    }

    /**
     * Generate SDF font with single contiguous range (for backward compatibility).
     */
    public SdfFont generate(ByteBuffer ttfData, int fontSize, int firstChar, int lastChar) {
        int[][] singleRange = {{firstChar, lastChar}};
        return generateWithRanges(ttfData, fontSize, singleRange, new int[0]);
    }

    /**
     * Generate SDF font with multiple character ranges and additional symbols.
     * This is the most flexible method allowing efficient packing of non-contiguous character sets.
     *
     * @param ttfData Font file data
     * @param fontSize Desired font size in pixels
     * @param charRanges Array of [start, end) ranges (end is exclusive)
     * @param extraChars Additional individual character codes to include
     * @return Generated SDF font
     */
    public SdfFont generateWithRanges(ByteBuffer ttfData, int fontSize, int[][] charRanges, int[] extraChars) {
        // Calculate total character count
        int charCount = 0;
        for (int[] range : charRanges) {
            charCount += range[1] - range[0];
        }
        charCount += extraChars.length;

        LOG.debug("Generating SDF font at size {0} with {1} ranges + {2} extra chars = {3} total characters",
                  fontSize, charRanges.length, extraChars.length, charCount);


        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Initialize font info
            STBTTFontinfo fontInfo = STBTTFontinfo.create();
            if (!stbtt_InitFont(fontInfo, ttfData)) {
                throw new RuntimeException("Failed to initialize TrueType font");
            }

            // Calculate scale factor for desired font size
            float scale = stbtt_ScaleForPixelHeight(fontInfo, fontSize);

            // Get font metrics
            IntBuffer ascent = stack.mallocInt(1);
            IntBuffer descent = stack.mallocInt(1);
            IntBuffer lineGap = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap);

            int ascentPixels = (int) (ascent.get(0) * scale);
            int lineHeight = (int) ((ascent.get(0) - descent.get(0) + lineGap.get(0)) * scale);

            // SDF parameters
            int sdfPadding = 4; // SDF distance field padding (added by stbtt_GetGlyphSDF)
            int padding = 1;   // Extra spacing between glyphs in atlas (minimal for packing efficiency)

            // Build complete list of characters to generate
            int[] allChars = new int[charCount];
            int charIndex = 0;

            // Add characters from ranges
            for (int[] range : charRanges) {
                for (int ch = range[0]; ch < range[1]; ch++) {
                    allChars[charIndex++] = ch;
                }
            }

            // Add extra characters
            for (int ch : extraChars) {
                allChars[charIndex++] = ch;
            }

            // First pass: measure all glyphs to calculate atlas size (no bitmap generation yet)
            GlyphSize[] sizes = new GlyphSize[charCount];

            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer xoff = stack.mallocInt(1);
            IntBuffer yoff = stack.mallocInt(1);

            for (int i = 0; i < charCount; i++) {
                int ch = allChars[i];
                int glyphIndex = stbtt_FindGlyphIndex(fontInfo, ch);
                if (glyphIndex == 0 && ch != ' ') {
                    sizes[i] = null;
                    continue;
                }

                // Generate SDF ONLY to measure dimensions, then immediately free it
                ByteBuffer glyphBitmap = stbtt_GetGlyphSDF(fontInfo,
                    scale,
                    glyphIndex,
                    sdfPadding,
                    (byte) 150,
                    (byte) (255 / (sdfPadding * 2)),
                    width, height,
                    xoff, yoff);

                if (glyphBitmap != null) {
                    sizes[i] = new GlyphSize(
                        width.get(0),
                        height.get(0),
                        xoff.get(0),
                        yoff.get(0)
                    );
                    stbtt_FreeSDF(glyphBitmap, 0L); // Free immediately after measuring
                }
            }

            // Calculate atlas size with row-based packing
            long totalArea = 0;
            for (GlyphSize s : sizes) {
                if (s != null) {
                    totalArea += (long) (s.width + padding) * (s.height + padding);
                }
            }
            int estimatedSize = (int) Math.sqrt(totalArea * 1.2); // 20% overhead
            int atlasWidth = nextPowerOfTwo(estimatedSize);
            int atlasHeight = nextPowerOfTwo(estimatedSize);

            LOG.debug("Estimated atlas size: {0}x{1}", atlasWidth, atlasHeight);

            // Create atlas bitmap (RGBA for better compatibility)
            ByteBuffer atlasData = ByteBuffer.allocateDirect(atlasWidth * atlasHeight * 4);

            // Create font object with ascent for baseline calculations
            Texture atlasTexture = createAtlasTexture(atlasWidth, atlasHeight);
            SdfFont font = new SdfFont(atlasTexture, fontSize, lineHeight, atlasWidth, atlasHeight, ascentPixels);

            // Second pass: regenerate and pack glyphs into atlas
            int x = 0, y = 0;
            int rowHeight = 0;

            IntBuffer advanceWidth = stack.mallocInt(1);
            IntBuffer leftSideBearing = stack.mallocInt(1);

            for (int i = 0; i < charCount; i++) {
                if (sizes[i] == null) continue;

                GlyphSize size = sizes[i];
                int ch = allChars[i];
                int glyphIndex = stbtt_FindGlyphIndex(fontInfo, ch);

                // Row wrapping: if glyph doesn't fit in current row, move to next
                if (x + size.width + padding > atlasWidth) {
                    x = 0;
                    y += rowHeight + padding;
                    rowHeight = 0;
                }

                // Ensure we have enough height
                if (y + size.height + padding > atlasHeight) {
                    throw new RuntimeException("Font atlas exceeded maximum height; consider reducing font size or charset");
                }

                // Regenerate the SDF bitmap for this glyph
                ByteBuffer glyphBitmap = stbtt_GetGlyphSDF(fontInfo,
                    scale,
                    glyphIndex,
                    sdfPadding,
                    (byte) 150,
                    (byte) (255 / (sdfPadding * 2)),
                    width, height,
                    xoff, yoff);

                if (glyphBitmap != null) {
                    // Copy glyph to atlas immediately
                    copyGlyphToAtlas(atlasData, atlasWidth, x, y, size.width, size.height, glyphBitmap);

                    // Free the bitmap immediately after copying
                    stbtt_FreeSDF(glyphBitmap, 0L);
                }

                // Get advance width for the glyph
                stbtt_GetGlyphHMetrics(fontInfo, glyphIndex, advanceWidth, leftSideBearing);

                // Create glyph metadata
                SdfGlyph glyph = new SdfGlyph(
                    (char) ch,
                    x,
                    y,
                    size.width,
                    size.height,
                    size.xoff,
                    size.yoff,
                    (int) (advanceWidth.get(0) * scale)
                );
                font.addGlyph(glyph);

                // Update row tracking
                x += size.width + padding;
                rowHeight = Math.max(rowHeight, size.height);
            }

            // Upload atlas to GPU
            updateAtlasTexture(atlasTexture, atlasData, atlasWidth, atlasHeight);

            LOG.debug("SDF font generation complete: {0}x{1} atlas with row-based packing", atlasWidth, atlasHeight);
            return font;
        }
    }

    /**
     * Simple data class to store glyph size info (no bitmap reference).
     */
    private static class GlyphSize {
        final int width;
        final int height;
        final int xoff;
        final int yoff;

        GlyphSize(int width, int height, int xoff, int yoff) {
            this.width = width;
            this.height = height;
            this.xoff = xoff;
            this.yoff = yoff;
        }
    }

    private static void copyGlyphToAtlas(ByteBuffer atlas, int atlasWidth, int x, int y, int gw, int gh, ByteBuffer glyphBitmap) {
        for (int row = 0; row < gh; row++) {
            for (int col = 0; col < gw; col++) {
                int atlasIndex = ((y + row) * atlasWidth + (x + col)) * 4;
                int glyphIndex = row * gw + col;

                if (glyphIndex < glyphBitmap.limit() && atlasIndex + 3 < atlas.capacity()) {
                    byte alpha = glyphBitmap.get(glyphIndex);
                    atlas.put(atlasIndex + 0, (byte) 255); // R
                    atlas.put(atlasIndex + 1, (byte) 255); // G
                    atlas.put(atlasIndex + 2, (byte) 255); // B
                    atlas.put(atlasIndex + 3, alpha);       // A
                }
            }
        }
    }

    private static Texture createAtlasTexture(int width, int height) {
        int textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        // SDF textures should NOT use mipmaps!
        // Mipmaps average/blur the distance field values, destroying the SDF information
        // and causing jagged rendering at small scales. The distance field itself handles scaling.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        return new GLTexture(textureId, width, height);
    }

    private static void updateAtlasTexture(Texture texture, ByteBuffer data, int width, int height) {
        data.flip();
        glBindTexture(GL_TEXTURE_2D, ((GLTexture) texture).getId());
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height,
            0, GL_RGBA, GL_UNSIGNED_BYTE, data);

        // DO NOT generate mipmaps for SDF textures!
        // Mipmapping destroys the distance field information
    }

    private static int nextPowerOfTwo(int value) {
        int power = 1;
        while (power < value) {
            power *= 2;
        }
        return power;
    }
}
