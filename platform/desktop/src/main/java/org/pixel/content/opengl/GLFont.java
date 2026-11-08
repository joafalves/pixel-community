package org.pixel.content.opengl;

import java.util.Map;

import org.pixel.content.Font;
import org.pixel.content.FontGlyph;

/**
 * Bitmap font whose glyphs live in an OpenGL texture atlas.
 */
public class GLFont extends Font {

    private final GLTexture texture;

    /**
     * Constructor.
     *
     * @param texture  The glyph atlas (owned by this font from now on).
     * @param fontSize The font size the atlas was rasterized at.
     * @param glyphs   The glyph metrics.
     */
    public GLFont(GLTexture texture, int fontSize, Map<Character, FontGlyph> glyphs) {
        super(fontSize, glyphs);
        if (texture == null) {
            throw new IllegalArgumentException("Font texture cannot be null.");
        }
        this.texture = texture;
    }

    @Override
    public long getMemorySize() {
        return texture.getMemorySize();
    }

    @Override
    public GLTexture getTexture() {
        return texture;
    }

    public int getTextureId() {
        return texture.getId();
    }

    /**
     * @return The (square) atlas size in pixels.
     */
    public int getTextureSize() {
        return texture.getWidth();
    }

    @Override
    public void dispose() {
        texture.dispose(); // idempotent
    }
}
