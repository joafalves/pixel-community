/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.content;

import static org.lwjgl.opengl.GL11C.glDeleteTextures;

import org.pixel.commons.lifecycle.Disposable;

public class Texture implements Disposable {

    //region properties

    private final int id;
    protected float width;
    protected float height;

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
     * Constructor.
     *
     * @param id     The native texture id.
     * @param width  The width of the texture.
     * @param height The height of the texture.
     */
    public Texture(int id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }

    /**
     * Disposes the Texture (attention: this will unbind the rendering texture from memory).
     */
    @Override
    public void dispose() {
        if (this.id >= 0) {
            glDeleteTextures(this.id);
        }
    }

    //endregion

}
