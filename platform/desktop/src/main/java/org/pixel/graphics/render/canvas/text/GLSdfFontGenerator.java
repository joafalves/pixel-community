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
import static org.lwjgl.opengl.GL30.*;

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
    private static final String ASCII_CHARSET = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

    @Override
    public SdfFont generate(ByteBuffer ttfData, int fontSize) {
        return generate(ttfData, fontSize, ASCII_CHARSET);
    }

    @Override
    public SdfFont generate(ByteBuffer ttfData, int fontSize, String charset) {
        LOG.debug("Generating SDF font at size {0} with {1} characters", fontSize, charset.length());

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

            // Calculate atlas size (simple square packing for now)
            // SDF adds padding on all sides, so glyphs are larger than base font size
            int padding = 4; // Extra spacing between glyphs in atlas
            int sdfPadding = 4; // SDF distance field padding (added by stbtt_GetGlyphSDF)
            int glyphsPerRow = 16;
            int maxGlyphSize = fontSize + sdfPadding * 2 + padding * 2; // Account for SDF padding + spacing
            int atlasWidth = maxGlyphSize * glyphsPerRow;
            int atlasHeight = maxGlyphSize * ((charset.length() + glyphsPerRow - 1) / glyphsPerRow);

            // Ensure power of 2 for better GPU compatibility
            atlasWidth = nextPowerOfTwo(atlasWidth);
            atlasHeight = nextPowerOfTwo(atlasHeight);

            // Create atlas bitmap (RGBA for better compatibility)
            ByteBuffer atlasData = ByteBuffer.allocateDirect(atlasWidth * atlasHeight * 4);

            // Create font object with ascent for baseline calculations
            Texture atlasTexture = createAtlasTexture(atlasWidth, atlasHeight);
            SdfFont font = new SdfFont(atlasTexture, fontSize, lineHeight, atlasWidth, atlasHeight, ascentPixels);

            // Prepare buffers for SDF generation
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer xoff = stack.mallocInt(1);
            IntBuffer yoff = stack.mallocInt(1);

            // Generate each glyph
            int x = 0, y = 0;
            for (int i = 0; i < charset.length(); i++) {
                char ch = charset.charAt(i);

                // Get glyph index
                int glyphIndex = stbtt_FindGlyphIndex(fontInfo, ch);
                if (glyphIndex == 0 && ch != ' ') continue; // Glyph not in font (allow space)

                // Get glyph metrics
                IntBuffer advanceWidth = stack.mallocInt(1);
                IntBuffer leftSideBearing = stack.mallocInt(1);
                stbtt_GetGlyphHMetrics(fontInfo, glyphIndex, advanceWidth, leftSideBearing);

                // Generate SDF bitmap using STB's SDF function
                // onedge_value: value at the edge (128 = middle of 0-255 range)
                // pixel_dist_scale: how many SDF gradient steps per pixel
                //   - This should match the SDF padding for best results
                ByteBuffer glyphBitmap = stbtt_GetGlyphSDF(fontInfo, 
                    scale,
                    glyphIndex, 
                    sdfPadding,  // 4 pixels of padding
                    (byte) 150,  // on-edge value (0-255)
                    (byte) (255 / (sdfPadding * 2)),  // Scale: 255 / 8 = ~32
                    width, height,
                    xoff, yoff);

                if (glyphBitmap != null) {
                    int finalWidth = width.get(0);
                    int finalHeight = height.get(0);
                    
                    // Copy SDF glyph to atlas
                    copyGlyphToAtlas(atlasData, atlasWidth, x + padding, y + padding, 
                        finalWidth, finalHeight, glyphBitmap);
                    
                    // Create glyph metadata - use xoff/yoff from SDF generation
                    // The xoff/yoff from STB already include the SDF padding (distance field border)
                    // However, they're in font space, so we need to account for that
                    SdfGlyph glyph = new SdfGlyph(
                        ch,
                        x + padding,
                        y + padding,
                        finalWidth,
                        finalHeight,
                        xoff.get(0),  // SDF offset X (includes padding, in pixels)
                        yoff.get(0),  // SDF offset Y (includes padding, in pixels)
                        (int) (advanceWidth.get(0) * scale)
                    );
                    font.addGlyph(glyph);
                    
                    // Free the SDF bitmap
                    stbtt_FreeSDF(glyphBitmap, 0L);
                }

                // Advance position in atlas
                x += maxGlyphSize;
                if (x + maxGlyphSize > atlasWidth) {
                    x = 0;
                    y += maxGlyphSize;
                }
            }

            // Upload atlas to GPU
            updateAtlasTexture(atlasTexture, atlasData, atlasWidth, atlasHeight);

            LOG.debug("SDF font generation complete: {0}x{1} atlas", atlasWidth, atlasHeight);
            return font;
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
        
        // Use linear filtering for smooth text at any scale
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
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
        
        // Generate mipmaps for better quality at different sizes
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    private static int nextPowerOfTwo(int value) {
        int power = 1;
        while (power < value) {
            power *= 2;
        }
        return power;
    }
}
