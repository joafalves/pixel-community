/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.io.Serializable;
import java.nio.FloatBuffer;

public class Matrix2 implements Serializable {

    //region properties

    private float[][] m = new float[2][2];
    private float[][] tmp = new float[2][2];

    //endregion

    //region constructors

    /**
     * Matrix with identity values
     */
    public Matrix2() {
        setIdentity();
    }

    //endregion

    //region private methods

    private void assignFromTmp() {
        m[0][0] = tmp[0][0];
        m[1][0] = tmp[1][0];

        m[0][1] = tmp[0][1];
        m[1][1] = tmp[1][1];
    }

    //endregion

    //region public methods

    /**
     * Sets the values to a identity matrix
     */
    public void setIdentity() {
        m[0][0] = 1.0f;
        m[1][1] = 1.0f;
        m[0][1] = 0.0f;
        m[1][0] = 0.0f;
    }

    /**
     * @param matrix
     */
    public void add(Matrix2 matrix) {
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[0].length; j++) {
                m[i][j] += matrix.m[i][j];
            }
        }
    }

    /**
     * @param matrix
     */
    public void subtract(Matrix2 matrix) {
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[0].length; j++) {
                m[i][j] -= matrix.m[i][j];
            }
        }
    }

    /**
     * @param matrix
     */
    public void multiply(Matrix2 matrix) {
        tmp[0][0] = m[0][0] * matrix.m[0][0] + m[0][1] * matrix.m[1][0];
        tmp[1][0] = m[1][0] * matrix.m[0][0] + m[1][1] * matrix.m[1][0];

        tmp[0][1] = m[0][0] * matrix.m[0][1] + m[0][1] * matrix.m[1][1];
        tmp[1][1] = m[1][0] * matrix.m[0][1] + m[1][1] * matrix.m[1][1];

        assignFromTmp();
    }

    /**
     * @param scalar
     */
    public void multiply(float scalar) {
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[0].length; ++j) {
                m[i][j] *= scalar;
            }
        }
    }

    /**
     *
     */
    public void negate() {
        multiply(-1.0f);
    }

    /**
     *
     */
    public void transpose() {
        float o10 = m[1][0];
        m[1][0] = m[0][1];
        m[0][1] = o10;
    }

    /**
     * @return
     */
    public Matrix2 clone() {
        Matrix2 matrix = new Matrix2();

        // straight clone doesn't work here due to the array having multiple dimensions..
        for (int i = 0; i < m.length; ++i) {
            matrix.m[i] = m[i].clone();
        }

        return matrix;
    }

    /**
     * Clones and returns the array value of the matrix
     *
     * @return
     */
    public float[][] toArray() {
        return m.clone();
    }

    /**
     * Returns the original array value of the matrix
     *
     * @return
     */
    public float[][] toUnsafeArray() {
        return m;
    }

    /**
     * @param buffer
     */
    public void writeBuffer(FloatBuffer buffer) {
        buffer.put(m[0][0]).put(m[1][0]);
        buffer.put(m[0][1]).put(m[1][1]);
        buffer.flip();
    }

    //endregion
}
