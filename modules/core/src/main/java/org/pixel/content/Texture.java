/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import org.pixel.commons.annotations.Nullable;
import org.pixel.commons.lifecycle.Disposable;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Texture implements Disposable {

    //region properties

    //private ByteBuffer imageData;
    private float width;
    private float height;
    private int id;

    //endregion

    //region getters & setters

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public int getId() {
        return id;
    }

    //endregion

    //region constructors

    /**
     * Constructor
     *
     * @param id
     */
    public Texture(int id) {
        this.id = id;
    }

    /**
     *
     */
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    /**
     * @param data
     * @param width
     * @param height
     */
    public void setData(@Nullable ByteBuffer data, int width, int height) {
        this.width = width;
        this.height = height;

        // upload image/info to the GPU:
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
    }

    /**
     * @param wrapS
     * @param wrapT
     */
    public void setTextureWrap(int wrapS, int wrapT) {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);
    }

    /**
     * @param minFilter
     * @param magFilter
     */
    public void setTextureMinMag(int minFilter, int magFilter) {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
    }

    /**
     * Unbind texture
     */
    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Disposes the Texture (attention: this will unbind the rendering texture from memory)
     */
    @Override
    public void dispose() {
        if (this.id >= 0) {
            // TODO: check how to sync with resource manager
            glDeleteTextures(this.id);
        }
    }

    //endregion

}
