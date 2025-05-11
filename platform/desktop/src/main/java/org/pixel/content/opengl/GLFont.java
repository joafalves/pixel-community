package org.pixel.content.opengl;

import static org.lwjgl.opengl.GL11C.*;

import java.util.Map;

import org.pixel.commons.lifecycle.State;
import org.pixel.content.Font;
import org.pixel.content.FontGlyph;

public class GLFont extends Font {

    private final int textureId;
    private final int textureSize;

    private State state = State.NEW;

    /**
     * Constructor for GLFont.
     *
     * @param textureId   The OpenGL texture ID.
     * @param textureSize The size of the texture.
     * @param fontSize    The size of the font.
     * @param glyphs      A map of characters to their corresponding FontGlyph objects.
     */
    public GLFont(int textureId, int textureSize, int fontSize, Map<Character, FontGlyph> glyphs) {
        super(fontSize, glyphs);
        this.textureId = textureId;
        this.textureSize = textureSize;
    }

    public int getTextureId() {
        return textureId;
    }

    public int getTextureSize() {
        return textureSize;
    }

    @Override
    public void dispose() {
        if (state.isDisposed()) {
            return;
        }

        if (this.textureId >= 0) {
            glDeleteTextures(this.textureId);
        }

        this.state = State.DISPOSED;
    }
}
