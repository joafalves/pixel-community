package org.pixel.content.opengl;

import android.opengl.GLES30;

import org.pixel.commons.lifecycle.State;
import org.pixel.content.Texture;

public class GLES30Texture extends Texture {

    private final int id;
    private State state = State.NEW;

    /**
     * Constructor
     *
     * @param id     The native texture id
     * @param width  The texture width
     * @param height The texture height
     */
    public GLES30Texture(int id, int width, int height) {
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
            int[] textureIds = new int[]{this.id};
            GLES30.glDeleteTextures(1, textureIds, 0);
        }

        this.state = State.DISPOSED;

    }
}
