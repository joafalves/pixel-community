package org.pixel.content.opengl;

import static org.lwjgl.opengl.GL11C.glDeleteTextures;

import org.pixel.content.Texture;

public class GLTexture extends Texture {

    public GLTexture(int id) {
        super(id);
    }

    /**
     * Constructor
     * 
     * @param id     The native texture id
     * @param width  The texture width
     * @param height The texture height
     */
    public GLTexture(int id, int width, int height) {
        super(id, width, height);
    }

    @Override
    public void dispose() {
        if (this.id >= 0) {
            glDeleteTextures(this.id);
            this.id = -1;
        }
    }
}
