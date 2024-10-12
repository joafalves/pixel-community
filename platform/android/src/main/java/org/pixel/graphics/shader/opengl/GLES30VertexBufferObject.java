/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.opengl;

import android.opengl.GLES30;
import org.pixel.commons.lifecycle.Disposable;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GLES30VertexBufferObject implements Disposable {

    //region private properties

    private final int id;

    //endregion

    //region constructors

    /**
     * Constructor.
     */
    public GLES30VertexBufferObject() {
        int[] vbo = new int[1];
        GLES30.glGenBuffers(1, vbo, 0);
        this.id = vbo[0];
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
        GLES30.glBindBuffer(target, id);
    }

    /**
     * Upload sub-data to the VBO.
     *
     * @param target The target.
     * @param offset The offset.
     * @param data   The data.
     */
    public void uploadSubData(int target, int offset, FloatBuffer data) {
        GLES30.glBufferSubData(target, offset, data.remaining() * Float.BYTES, data);
    }

    /**
     * Upload data to the VBO.
     *
     * @param target The target.
     * @param data   The data.
     * @param usage  The usage.
     */
    public void uploadData(int target, FloatBuffer data, int usage) {
        GLES30.glBufferData(target, data.remaining() * Float.BYTES, data, usage);
    }

    /**
     * Upload data to the VBO.
     *
     * @param target The target.
     * @param data   The data.
     * @param usage  The usage.
     */
    public void uploadData(int target, IntBuffer data, int usage) {
        GLES30.glBufferData(target, data.remaining() * Float.BYTES, data, usage);
    }

    @Override
    public void dispose() {
        GLES30.glDeleteBuffers(1, new int[]{id}, 0);
    }

    //endregion
}
