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
     * @param premultiplied Whether the pixels hold premultiplied colors (render targets); the renderer then
     *                      un-premultiplies when sampling, so the texture blends like any other.
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
