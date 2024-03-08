package org.pixel.content.opengl;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.opengl.GL11C.GL_LINEAR;
import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_RGBA8;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glDeleteTextures;
import static org.lwjgl.opengl.GL11C.glGenTextures;
import static org.lwjgl.opengl.GL11C.glTexImage2D;
import static org.lwjgl.opengl.GL11C.glTexParameteri;
import static org.lwjgl.stb.STBTruetype.stbtt_PackBegin;
import static org.lwjgl.stb.STBTruetype.stbtt_PackEnd;
import static org.lwjgl.stb.STBTruetype.stbtt_PackFontRange;
import static org.lwjgl.stb.STBTruetype.stbtt_PackSetOversampling;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;

import org.lwjgl.stb.STBTTPackContext;
import org.pixel.content.Font;
import org.pixel.content.FontData;


public class GLFont extends Font {

    public GLFont(FontData fontData) {
        super(fontData);
    }
    
    public GLFont(FontData fontData, int fontSize, int horizontalSpacing, int verticalSpacing, int oversampling) {
        super(fontData, fontSize, horizontalSpacing, verticalSpacing, oversampling);
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
        // note: since stbtt optimizes the glyph placing on the bitmap, we don't need to go for 1:1 ratio here
        float maxGlyphSize = this.fontSize * oversampling;
        float tmpTextureSize = (maxGlyphSize + GLYPH_TEXTURE_PADDING_COMPENSATION) * 8;
        if (tmpTextureSize % 2 != 0) {
            tmpTextureSize++; // let's keep this in multiples of 2
        }

        this.textureWidth = (int) tmpTextureSize;
        this.textureHeight = (int) tmpTextureSize;

        glyphCache.clear(); // clear the glyph cache
        packedBuffer.clear(); // clear char data buffer
        STBTTPackContext pc = STBTTPackContext.malloc();
        ByteBuffer alphaBitmap = createByteBuffer(this.textureWidth * this.textureHeight);
        stbtt_PackBegin(pc, alphaBitmap, this.textureWidth, this.textureHeight, 0, GLYPH_TEXTURE_PADDING, NULL);
        // load up data to our buffer:
        packedBuffer.limit(255); // text ascii range (32-127)
        packedBuffer.position(32);
        stbtt_PackSetOversampling(pc, oversampling, oversampling);
        stbtt_PackFontRange(pc, fontData.getSource(), 0, getFontSize(), 32, packedBuffer);
        stbtt_PackEnd(pc);
        packedBuffer.clear();

        // convert gray scale to rgba
        bitmap = createByteBuffer(alphaBitmap.limit() * 4);
        for (int i = 0; i < alphaBitmap.limit(); ++i) {
            bitmap.put((byte) 255);
            bitmap.put((byte) 255);
            bitmap.put((byte) 255);
            bitmap.put(alphaBitmap.get());
        }
        bitmap.clear();

        // bind char data to our texture:
        glBindTexture(GL_TEXTURE_2D, getTextureId());
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, this.textureWidth, this.textureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                bitmap);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    }

    @Override
    public void dispose() {
        if (this.textureId >= 0) {
            glDeleteTextures(this.textureId);
            this.textureId = -1;
        } 
    }
    
}
