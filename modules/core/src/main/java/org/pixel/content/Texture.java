/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import static org.lwjgl.opengl.GL11C.GL_RGBA;
import static org.lwjgl.opengl.GL11C.GL_RGBA8;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11C.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.glBindTexture;
import static org.lwjgl.opengl.GL11C.glDeleteTextures;
import static org.lwjgl.opengl.GL11C.glTexImage2D;
import static org.lwjgl.opengl.GL11C.glTexParameteri;

import java.nio.ByteBuffer;
import org.pixel.commons.annotations.Nullable;
import org.pixel.commons.lifecycle.Disposable;

public class Texture implements Disposable {

    //region properties

    private final int id;
    private float width;
    private float height;

    //endregion

    //region getters & setters

    /**
     * Get the width of the texture.
     *
     * @return the width of the texture.
     */
    public float getWidth() {
        return width;
    }

    /**
     * Get the height of the texture.
     *
     * @return the height of the texture.
     */
    public float getHeight() {
        return height;
    }

    /**
     * Get the native texture id.
     *
     * @return the native texture id.
     */
    public int getId() {
        return id;
    }

    //endregion

    //region constructors

    /**
     * Constructor.
     *
     * @param id The native texture id.
     */
    public Texture(int id) {
        this.id = id;
    }

    /**
     * Binds the texture to the current rendering context. This function must be called before rendering or updating the
     * texture properties.
     */
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    /**
     * Set the texture data from a byte buffer.
     *
     * @param data   The byte buffer containing the texture data.
     * @param width  The width of the texture.
     * @param height The height of the texture.
     */
    public void setData(@Nullable ByteBuffer data, int width, int height) {
        this.width = width;
        this.height = height;

        // upload image/info to the GPU:
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
    }

    /**
     * Set the texture wrapping mode.
     *
     * @param wrapS The horizontal wrapping mode.
     * @param wrapT The vertical wrapping mode.
     */
    public void setTextureWrap(int wrapS, int wrapT) {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);
    }

    /**
     * Set the texture filtering mode.
     *
     * @param minFilter The minification filter.
     * @param magFilter The magnification filter.
     */
    public void setTextureMinMag(int minFilter, int magFilter) {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter);
    }

    /**
     * Unbind texture from the current rendering context.
     */
    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Disposes the Texture (attention: this will unbind the rendering texture from memory).
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
