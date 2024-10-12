package org.pixel.content.opengl;

import android.opengl.GLES30;
import org.pixel.content.Texture;

public class GLES30Texture extends Texture {

    /**
     * Constructor
     *
     * @param id The native texture id
     */
    public GLES30Texture(int id) {
        super(id);
    }

    /**
     * Constructor
     *
     * @param id     The native texture id
     * @param width  The texture width
     * @param height The texture height
     */
    public GLES30Texture(int id, int width, int height) {
        super(id, width, height);
    }

    @Override
    public void dispose() {
        if (this.id >= 0) {
            int[] textureIds = new int[]{this.id};
            GLES30.glDeleteTextures(1, textureIds, 0);
            this.id = -1;
        }
    }
}
