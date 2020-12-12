/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.graphics.shader;

import pixel.commons.lifecycle.Disposable;

import static org.lwjgl.opengl.GL30.*;

public class VertexArrayObject implements Disposable {

    //region private properties

    private final int id;

    //endregion

    //region constructors

    /**
     * Constructor
     */
    public VertexArrayObject() {
        id = glGenVertexArrays();
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
        glBindVertexArray(id);
    }

    /**
     *
     */
    public void unbind() {
        glBindVertexArray(0);
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
