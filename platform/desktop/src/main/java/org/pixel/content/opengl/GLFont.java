package org.pixel.content.opengl;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.stb.STBImageWrite.stbi_write_bmp;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.stb.STBTruetype.stbtt_PackBegin;
import static org.lwjgl.stb.STBTruetype.stbtt_PackEnd;
import static org.lwjgl.stb.STBTruetype.stbtt_PackFontRange;
import static org.lwjgl.stb.STBTruetype.stbtt_PackSetOversampling;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;

import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.pixel.content.Font;
import org.pixel.content.FontData;
import org.pixel.content.FontGlyph;

public class GLFont extends Font {

    protected STBTTPackedchar.Buffer packedBuffer;

    public GLFont(FontData fontData) {
        super(fontData);
    }

    public GLFont(FontData fontData, int fontSize, int horizontalSpacing, int verticalSpacing, int oversampling) {
        super(fontData, fontSize, horizontalSpacing, verticalSpacing, oversampling);
    }

    @Override
    protected void init() {
        this.packedBuffer = STBTTPackedchar.malloc(6 * 128);
        super.init();
    }

    @Override
    protected void computeFontData() {
        if (fontData.getSource() == null) {
            throw new RuntimeException("Cannot compute font without a valid source");
        }

        if (this.getTextureId() < 0) {
            // texture is not yet assigned to this font, generate:
            this.textureId = glGenTextures();
        }

        // calculate texture size:
        float maxGlyphSize = this.fontSize * oversampling;
        int glyphsPerSide = (int)Math.sqrt(95); // 95 printable ASCII chars (32-127)
        int requiredSize = (int)(maxGlyphSize * glyphsPerSide * 1.2f); // 20% padding

        // Round up to nearest power of 2
        int textureSize = Integer.highestOneBit(requiredSize - 1) << 1;

        // Set both width and height to the same power-of-2 size
        this.textureWidth = textureSize;
        this.textureHeight = textureSize;

        glyphCache.clear(); // clear the glyph cache
        packedBuffer.clear(); // clear char data buffer

        STBTTPackContext pc = null;
        try {
            pc = STBTTPackContext.malloc();
            ByteBuffer alphaBitmap = createByteBuffer(this.textureWidth * this.textureHeight);
            stbtt_PackBegin(pc, alphaBitmap, this.textureWidth, this.textureHeight, 0, GLYPH_TEXTURE_PADDING, NULL);
            // load up data to our buffer:
            packedBuffer.limit(127); // text ascii range (32-127) - standard ascii
            packedBuffer.position(32); // first printable char
            stbtt_PackSetOversampling(pc, oversampling, oversampling);
            stbtt_PackFontRange(pc, fontData.getSource(), 0, getFontSize(), 32, packedBuffer);
            stbtt_PackEnd(pc);

            // convert gray scale to rgba bitmap (4 byte per pixel):
            bitmap = createByteBuffer(alphaBitmap.limit() * 4);
            for (int i = 0; i < alphaBitmap.limit(); ++i) {
                bitmap.put((byte) 255);
                bitmap.put((byte) 255);
                bitmap.put((byte) 255);
                bitmap.put((byte) (alphaBitmap.get() & 0xFF));
            }
            bitmap.clear();

            // bind char data to our texture:
            glBindTexture(GL_TEXTURE_2D, getTextureId());
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, this.textureWidth, this.textureHeight, 0, GL_RGBA,
                    GL_UNSIGNED_BYTE, bitmap);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

            // unbind the texture
            glBindTexture(GL_TEXTURE_2D, 0);

        } finally {
            if (pc != null) {
                pc.free();
            }
        }
    }

    @Override
    public void dispose() {
        if (this.textureId >= 0) {
            glDeleteTextures(this.textureId);
            this.textureId = -1;
        }
    }

    @Override
    public void saveAsPng(String filepath) {
        stbi_write_png(filepath, this.textureWidth, this.textureHeight, 4, bitmap, 0);
    }

    @Override
    public void saveAsBmp(String filepath) {
        stbi_write_bmp(filepath, this.textureWidth, this.textureHeight, 4, bitmap);
    }

    @Override
    public FontGlyph getGlyph(char ch) {
        FontGlyph glyph = this.glyphCache.get(ch);
        if (glyph == null) {
            STBTTPackedchar pc = this.packedBuffer.get(ch);
            if (ch == 9) {
                // tab
                return null;
            }

            // create a new font glyph
            glyph = new FontGlyph(pc.x0(), pc.y0(), pc.x1() - pc.x0(), pc.y1() - pc.y0(), pc.xadvance(),
                    pc.xoff(), pc.yoff());

            this.glyphCache.put(ch, glyph);
        }

        return glyph;
    }
}
