package org.pixel.content.opengl;

import org.pixel.content.Texture;
import org.pixel.graphics.opengl.GLApi;

/**
 * A texture stored in an OpenGL (ES) texture object; disposing it deletes the GL texture.
 */
public class GLTexture extends Texture {

    private final GLApi gl;
    private final int id;
    private boolean disposed;

    /**
     * @param gl     The GL functions (used to delete the texture).
     * @param id     The GL texture name (owned by this object from now on).
     * @param width  Width in pixels.
     * @param height Height in pixels.
     */
    public GLTexture(GLApi gl, int id, int width, int height) {
        super(width, height);
        if (gl == null) {
            throw new IllegalArgumentException("GL functions cannot be null.");
        }
        this.gl = gl;
        this.id = id;
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
