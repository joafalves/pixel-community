package org.pixel.content.opengl;

import lombok.Getter;
import org.pixel.commons.lifecycle.State;
import org.pixel.content.Texture;

import static org.lwjgl.opengl.GL11C.glDeleteTextures;
import static org.lwjgl.opengl.GL11C.glGenTextures;

@Getter
public class GLTexture extends Texture {

    private final int id;
    private State state = State.NEW;

    public GLTexture(int width, int height) {
        this(glGenTextures(), width, height);
    }

    public GLTexture(int id, int width, int height) {
        super(width, height);
        this.id = id;
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
