/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader;

import static org.lwjgl.opengl.GL30.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL30.glBindBuffer;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenBuffers;

import org.pixel.commons.lifecycle.Disposable;

public class ElementBufferObject implements Disposable {

    //region private properties

    private final int id;

    //endregion

    //region constructors

    /**
     * Constructor.
     */
    public ElementBufferObject() {
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
