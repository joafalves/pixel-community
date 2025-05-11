package org.pixel.content.opengl;

import static org.lwjgl.opengl.GL11C.glDeleteTextures;

import org.pixel.commons.lifecycle.State;
import org.pixel.content.Texture;

public class GLTexture extends Texture {

    private final int id;
    private State state = State.NEW;

    public GLTexture(int id, int width, int height) {
        super(width, height);
        this.id = id;
    }

    /**
     * Get the OpenGL texture ID.
     *
     * @return the OpenGL texture ID.
     */
    public int getId() {
        return id;
    }

    @Override
    public void dispose() {
        if (state.isDisposed()) {
            return;
        }

        if (this.id >= 0) {
            glDeleteTextures(this.id);
        }

        this.state = State.DISPOSED;
    }
}
