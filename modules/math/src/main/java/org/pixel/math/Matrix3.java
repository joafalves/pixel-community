/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.io.Serializable;
import java.nio.FloatBuffer;

public class Matrix3 implements Serializable {

    //region properties

    private float[][] m = new float[3][3];
    private float[][] tmp = new float[3][3];

    //endregion

    //region constructors

    /**
     * Matrix with identity values
     */
    public Matrix3() {
        setIdentity();
    }

    //endregion

    //region private methods

    private void assignFromTmp() {
        m[0][0] = tmp[0][0];
        m[1][0] = tmp[1][0];
        m[2][0] = tmp[2][0];

        m[0][1] = tmp[0][1];
        m[1][1] = tmp[1][1];
        m[2][1] = tmp[2][1];

        m[0][2] = tmp[0][2];
        m[1][2] = tmp[1][2];
        m[2][2] = tmp[2][2];
    }

    //endregion

    //region public methods

    /**
     * Sets the values to a identity matrix
     */
    public void setIdentity() {
        m[0][0] = 1.0f;
        m[1][1] = 1.0f;
        m[2][2] = 1.0f;
        m[0][1] = 0.0f;
        m[0][2] = 0.0f;
        m[1][0] = 0.0f;
        m[1][2] = 0.0f;
        m[2][0] = 0.0f;
        m[2][1] = 0.0f;
    }

    /**
     * @param matrix
     */
    public void add(Matrix3 matrix) {
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[0].length; ++j) {
                m[i][j] += matrix.m[i][j];
            }
        }
    }

    /**
     * @param matrix
     */
    public void subtract(Matrix3 matrix) {
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[0].length; ++j) {
                m[i][j] -= matrix.m[i][j];
            }
        }
    }

    /**
     * @param matrix
     */
    public void multiply(Matrix3 matrix) {
        tmp[0][0] = m[0][0] * matrix.m[0][0] + m[0][1] * matrix.m[1][0] + m[0][2] * matrix.m[2][0];
        tmp[1][0] = m[1][0] * matrix.m[0][0] + m[1][1] * matrix.m[1][0] + m[1][2] * matrix.m[2][0];
        tmp[2][0] = m[2][0] * matrix.m[0][0] + m[2][1] * matrix.m[1][0] + m[2][2] * matrix.m[2][0];

        tmp[0][1] = m[0][0] * matrix.m[0][1] + m[0][1] * matrix.m[1][1] + m[0][2] * matrix.m[2][1];
        tmp[1][1] = m[1][0] * matrix.m[0][1] + m[1][1] * matrix.m[1][1] + m[1][2] * matrix.m[2][1];
        tmp[2][1] = m[2][0] * matrix.m[0][1] + m[2][1] * matrix.m[1][1] + m[2][2] * matrix.m[2][1];

        tmp[0][2] = m[0][0] * matrix.m[0][2] + m[0][1] * matrix.m[1][2] + m[0][2] * matrix.m[2][2];
        tmp[1][2] = m[1][0] * matrix.m[0][2] + m[1][1] * matrix.m[1][2] + m[1][2] * matrix.m[2][2];
        tmp[2][2] = m[2][0] * matrix.m[0][2] + m[2][1] * matrix.m[1][2] + m[2][2] * matrix.m[2][2];

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
    public void invert() {
        throw new RuntimeException("Not implemented");
    }

    /**
     *
     */
    public void transpose() {
        tmp[0][0] = m[0][0];
        tmp[1][0] = m[0][1];
        tmp[2][0] = m[0][2];

        tmp[0][1] = m[1][0];
        tmp[1][1] = m[1][1];
        tmp[2][1] = m[1][2];

        tmp[0][2] = m[2][0];
        tmp[1][2] = m[2][1];
        tmp[2][2] = m[2][2];

        assignFromTmp();
    }

    /**
     * @return
     */
    public Matrix3 clone() {
        Matrix3 matrix = new Matrix3();

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
        buffer.put(m[0][0]).put(m[1][0]).put(m[2][0]);
        buffer.put(m[0][1]).put(m[1][1]).put(m[2][1]);
        buffer.put(m[0][2]).put(m[1][2]).put(m[2][2]);
        buffer.flip();
    }

    //endregion
}
