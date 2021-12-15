/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader;

import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.pixel.commons.lifecycle.Disposable;

public class VertexBufferObject implements Disposable {

    //region private properties

    private final int id;

    //endregion

    //region constructors

    /**
     * Constructor.
     */
    public VertexBufferObject() {
        id = glGenBuffers();
    }

    //endregion

    //region public methods

    /**
     * Get the VBO native id.
     *
     * @return The VBO native id.
     */
    public int getID() {
        return this.id;
    }

    /**
     * Bind the VBO.
     *
     * @param target The target.
     */
    public void bind(int target) {
        glBindBuffer(target, id);
    }

    /**
     * Upload sub-data to the VBO.
     *
     * @param target The target.
     * @param offset The offset.
     * @param data   The data.
     */
    public void uploadSubData(int target, long offset, FloatBuffer data) {
        glBufferSubData(target, offset, data);
    }

    /**
     * Upload data to the VBO.
     *
     * @param target The target.
     * @param data   The data.
     * @param usage  The usage.
     */
    public void uploadData(int target, FloatBuffer data, int usage) {
        glBufferData(target, data, usage);
    }

    /**
     * Upload data to the VBO.
     *
     * @param target The target.
     * @param data   The data.
     * @param usage  The usage.
     */
    public void uploadData(int target, IntBuffer data, int usage) {
        glBufferData(target, data, usage);
    }

    @Override
    public void dispose() {
        glDeleteBuffers(id);
    }

    //endregion
}
