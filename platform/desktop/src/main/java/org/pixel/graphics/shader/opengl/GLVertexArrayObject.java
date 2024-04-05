/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.opengl;

import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

import org.pixel.commons.lifecycle.Disposable;

public class GLVertexArrayObject implements Disposable {

    //region private properties

    private final int id;

    //endregion

    //region constructors

    /**
     * Constructor.
     */
    public GLVertexArrayObject() {
        id = glGenVertexArrays();
    }

    //endregion

    //region public methods

    /**
     * Get VAO native id.
     *
     * @return The VAO native id.
     */
    public int getID() {
        return this.id;
    }

    /**
     * Bind the VAO.
     */
    public void bind() {
        glBindVertexArray(id);
    }

    /**
     * Unbind the VAO.
     */
    public void unbind() {
        glBindVertexArray(0);
    }

    @Override
    public void dispose() {
        glDeleteVertexArrays(id);
    }

    //endregion
}
