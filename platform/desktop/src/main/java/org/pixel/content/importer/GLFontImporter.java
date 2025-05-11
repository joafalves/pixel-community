package org.pixel.content.importer;

import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.pixel.content.*;
import org.pixel.content.importer.settings.FontImporterSettings;
import org.pixel.content.opengl.GLFont;

import java.util.HashMap;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.stb.STBTruetype.stbtt_PackEnd;
import static org.lwjgl.system.MemoryUtil.NULL;

@ContentImporterInfo(type = Font.class, extension = {".ttf", ".otf"})
public class GLFontImporter implements ContentImporter<Font> {

    protected static final int GLYPH_TEXTURE_PADDING = 1;

    @Override
    public Font process(ImportContext ctx) {
        final var rawBuffer = createByteBuffer(ctx.getData().length);
        rawBuffer.put(ctx.getData()).flip(); // reset position to 0
        final var fontData = new FontData(rawBuffer);
        if (fontData.getSource() == null) {
            throw new IllegalArgumentException("Font source cannot be null.");
        }

        final var settings = ctx.getSettings() instanceof FontImporterSettings
                ? (FontImporterSettings) ctx.getSettings()
                : FontImporterSettings.builder().build();   // default settings

        // Create GL Texture:
        final var glTextureId = glGenTextures();

        // Compute GL Font:
        STBTTPackedchar.Buffer charBuffer = null;
        STBTTPackContext ctxBuffer = null;
        try {
            charBuffer = STBTTPackedchar.malloc(6 * 128);
            ctxBuffer = STBTTPackContext.malloc();

            final var textureSize = calculateTextureSize(settings);
            final var fontSize = settings.getFontSize();
            final var oversampling = settings.getOversampling();
            final var alphaBitmap = createByteBuffer(textureSize * textureSize);

            stbtt_PackBegin(ctxBuffer, alphaBitmap, textureSize, textureSize, 0, GLYPH_TEXTURE_PADDING, NULL);

            charBuffer.limit(127); // text ascii range (32-127) - standard ascii
            charBuffer.position(32); // first printable char
            stbtt_PackSetOversampling(ctxBuffer, oversampling, oversampling);
            stbtt_PackFontRange(ctxBuffer, fontData.getSource(), 0, fontSize, 32, charBuffer);
            stbtt_PackEnd(ctxBuffer);

            // Create an RGBA bitmap based on the alpha font bitmap:
            final var bitmap = createByteBuffer(alphaBitmap.limit() * 4);
            for (int i = 0; i < alphaBitmap.limit(); ++i) {
                bitmap.put((byte) 255);
                bitmap.put((byte) 255);
                bitmap.put((byte) 255);
                bitmap.put((byte) (alphaBitmap.get() & 0xFF));
            }
            bitmap.clear(); // reset position to 0 ("clear" is a weird name for this)

            // bind char data to our final texture:
            glBindTexture(GL_TEXTURE_2D, glTextureId);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, textureSize, textureSize, 0, GL_RGBA,
                    GL_UNSIGNED_BYTE, bitmap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            // unbind the texture
            glBindTexture(GL_TEXTURE_2D, 0);

            // Build the glyph map:
            final var glyphMap = new HashMap<Character, FontGlyph>();
            for (int i = 0; i < 127; i++) { // printable ASCII chars
                final var packedChar = charBuffer.get((char) i);
                glyphMap.put((char) i, FontGlyph.builder()
                        .x(packedChar.x0())
                        .y(packedChar.y0())
                        .width(packedChar.x1() - packedChar.x0())
                        .height(packedChar.y1() - packedChar.y0())
                        .xAdvance(packedChar.xadvance())
                        .xOffset(packedChar.xoff())
                        .yOffset(packedChar.yoff())
                        .build());
            }

            return new GLFont(glTextureId, textureSize, fontSize, glyphMap);

        } finally {
            // Free the allocated memory if an exception occurs
            if (charBuffer != null) {
                charBuffer.free();
            }
            if (ctxBuffer != null) {
                ctxBuffer.free();
            }
        }
    }

    private int calculateTextureSize(FontImporterSettings settings) {
        float maxGlyphSize = settings.getFontSize() * settings.getOversampling();
        int glyphsPerSide = (int) Math.sqrt(95); // 95 printable ASCII chars (32-127)
        int requiredSize = (int) (maxGlyphSize * glyphsPerSide * 1.2f); // 20% padding

        return Integer.highestOneBit(requiredSize - 1) << 1;
    }
}
