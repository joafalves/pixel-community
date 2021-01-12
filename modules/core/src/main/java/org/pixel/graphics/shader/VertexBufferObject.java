/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader;

import org.pixel.commons.lifecycle.Disposable;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;

public class VertexBufferObject implements Disposable {

    //region private properties

    private final int id;

    //endregion

    //region constructors

    /**
     * Constructor
     */
    public VertexBufferObject() {
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
     * @param target
     */
    public void bind(int target) {
        glBindBuffer(target, id);
    }

    /**
     * @param target
     * @param offset
     * @param data
     */
    public void uploadSubData(int target, long offset, FloatBuffer data) {
        glBufferSubData(target, offset, data);
    }

    /**
     * @param target
     * @param data
     * @param usage
     */
    public void uploadData(int target, FloatBuffer data, int usage) {
        glBufferData(target, data, usage);
    }

    /**
     * @param target
     * @param data
     * @param usage
     */
    public void uploadData(int target, IntBuffer data, int usage) {
        glBufferData(target, data, usage);
    }

    /**
     *
     */
    @Override
    public void dispose() {
        glDeleteBuffers(id);
    }

    //endregion
}
