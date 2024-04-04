/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.opengl;

import static org.lwjgl.opengl.GL30C.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL30C.glBindBuffer;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glGenBuffers;

import org.pixel.commons.lifecycle.Disposable;

public class GLElementBufferObject implements Disposable {

    //region private properties

    private final int id;

    //endregion

    //region constructors

    /**
     * Constructor.
     */
    public GLElementBufferObject() {
        id = glGenBuffers();
    }

    //endregion

    //region public methods

    /**
     * Get the EBO native id.
     *
     * @return the EBO native id.
     */
    public int getID() {
        return this.id;
    }

    /**
     * Binds the EBO.
     */
    public void bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
    }

    /**
     * Unbinds the EBO.
     */
    public void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void dispose() {
        glDeleteVertexArrays(id);
    }

    //endregion
}
