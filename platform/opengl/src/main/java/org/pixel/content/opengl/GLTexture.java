package org.pixel.content.opengl;

import org.pixel.content.Texture;
import org.pixel.graphics.opengl.GLApi;

/**
 * A texture stored in an OpenGL (ES) texture object; disposing it deletes the GL texture.
 */
public class GLTexture extends Texture {

    private final GLApi gl;
    private final int id;
    private final boolean premultiplied;
    private boolean disposed;

    /**
     * @param gl     The GL functions (used to delete the texture).
     * @param id     The GL texture name (owned by this object from now on).
     * @param width  Width in pixels.
     * @param height Height in pixels.
     */
    public GLTexture(GLApi gl, int id, int width, int height) {
        this(gl, id, width, height, false);
    }

    /**
     * @param premultiplied Whether the pixels hold premultiplied colors (images as imported, render targets). The
     *                      renderer works in premultiplied colors: it uses such texels as they are, and premultiplies
     *                      the others (e.g. font atlases, whose channels are distances) when it samples them as
     *                      colors.
     */
    public GLTexture(GLApi gl, int id, int width, int height, boolean premultiplied) {
        super(width, height);
        if (gl == null) {
            throw new IllegalArgumentException("GL functions cannot be null.");
        }
        this.gl = gl;
        this.id = id;
        this.premultiplied = premultiplied;
    }

    /**
     * Premultiply RGBA8 pixels in place ({@code rgb *= a}), so linear filtering never mixes in the color of
     * transparent texels (no dark or bright fringes around smoothly scaled sprites).
     *
     * @param rgba The pixels, from position to limit.
     */
    public static void premultiply(java.nio.ByteBuffer rgba) {
        for (int i = rgba.position(), end = rgba.limit() - 3; i < end; i += 4) {
            int a = rgba.get(i + 3) & 0xFF;
            if (a == 255) {
                continue;
            }
            for (int c = 0; c < 3; c++) {
                int v = rgba.get(i + c) & 0xFF;
                rgba.put(i + c, (byte) ((v * a + 127) / 255));
            }
        }
    }

    /**
     * @return Whether the pixels hold premultiplied colors.
     */
    public boolean isPremultiplied() {
        return premultiplied;
    }

    /** Called by render targets when they reallocate their storage. */
    protected void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    /**
     * @return The GL texture name.
     */
    public int getId() {
        return id;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            gl.deleteTexture(id);
        }
    }
}
