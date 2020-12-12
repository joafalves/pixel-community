/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.graphics.shader;

import pixel.commons.lifecycle.Disposable;

import static org.lwjgl.opengl.GL30.*;

public class ElementBufferObject implements Disposable {

    //region private properties

    private final int id;

    //endregion

    //region constructors

    /**
     * Constructor
     */
    public ElementBufferObject() {
        id = glGenBuffers();
    }

    //endregion

    //region public methods

    /**
     * @return
     */
    public int getID() {
        return this.id;
    }

    /**
     *
     */
    public void bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
    }

    /**
     *
     */
    public void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     *
     */
    @Override
    public void dispose() {
        glDeleteVertexArrays(id);
    }

    //endregion
}
