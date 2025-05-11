package org.pixel.content.opengl;

import static org.lwjgl.opengl.GL11C.*;

import java.util.Map;

import org.pixel.content.Font;
import org.pixel.content.FontGlyph;

public class GLFont extends Font {

    public GLFont(int textureId, int textureSize, int fontSize, Map<Character, FontGlyph> glyphs) {
        super(textureId, textureSize, fontSize, glyphs);
    }

    @Override
    public void dispose() {
        if (this.textureId >= 0) {
            glDeleteTextures(this.textureId);
        }
    }
}
