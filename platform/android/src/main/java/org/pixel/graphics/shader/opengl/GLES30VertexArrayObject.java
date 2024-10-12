/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.opengl;

import android.opengl.GLES30;
import org.pixel.commons.lifecycle.Disposable;

public class GLES30VertexArrayObject implements Disposable {

    //region private properties

    private final int id;

    //endregion

    //region constructors

    /**
     * Constructor.
     */
    public GLES30VertexArrayObject() {
        int[] vao = new int[1];
        GLES30.glGenVertexArrays(1, vao, 0);
        this.id = vao[0];
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
        GLES30.glBindVertexArray(id);
    }

    /**
     * Unbind the VAO.
     */
    public void unbind() {
        GLES30.glBindVertexArray(0);
    }

    @Override
    public void dispose() {
        GLES30.glDeleteVertexArrays(1, new int[]{id}, 0);
    }

    //endregion
}
