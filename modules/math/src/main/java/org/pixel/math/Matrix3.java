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
     * Constructor. Matrix with identity values.
     */
    public Matrix3() {
        setIdentity();
    }

    /**
     * Constructor.
     * @param other The matrix to copy values from.
     */
    public Matrix3(Matrix3 other) {
        this.m[0][0] = other.m[0][0];
        this.m[0][1] = other.m[0][1];
        this.m[0][2] = other.m[0][2];
        this.m[1][0] = other.m[1][0];
        this.m[1][1] = other.m[1][1];
        this.m[1][2] = other.m[1][2];
        this.m[2][0] = other.m[2][0];
        this.m[2][1] = other.m[2][1];
        this.m[2][2] = other.m[2][2];
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
     * Sets the values to an identity matrix.
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
     * Matrix addition.
     *
     * @param matrix The matrix to add.
     */
    public void add(Matrix3 matrix) {
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[0].length; ++j) {
                m[i][j] += matrix.m[i][j];
            }
        }
    }

    /**
     * Matrix subtraction.
     *
     * @param matrix The matrix to subtract.
     */
    public void subtract(Matrix3 matrix) {
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[0].length; ++j) {
                m[i][j] -= matrix.m[i][j];
            }
        }
    }

    /**
     * Matrix multiplication.
     *
     * @param matrix The matrix to multiply with.
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
     * Multiplies the matrix by a scalar.
     *
     * @param scalar The scalar to multiply with.
     */
    public void multiply(float scalar) {
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[0].length; ++j) {
                m[i][j] *= scalar;
            }
        }
    }

    /**
     * Multiples each value on the matrix by -1.
     */
    public void negate() {
        multiply(-1.0f);
    }

    /**
     * Transposes the matrix.
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
     * Clones and returns the array value of the matrix.
     *
     * @return The cloned array.
     */
    public float[][] toArray() {
        return m.clone();
    }

    /**
     * Returns the original array value of the matrix.
     *
     * @return The original array.
     */
    public float[][] toUnsafeArray() {
        return m;
    }

    /**
     * Write matrix values to given float buffer.
     *
     * @param buffer The float buffer to write to.
     */
    public void writeBuffer(FloatBuffer buffer) {
        buffer.put(m[0][0]).put(m[1][0]).put(m[2][0]);
        buffer.put(m[0][1]).put(m[1][1]).put(m[2][1]);
        buffer.put(m[0][2]).put(m[1][2]).put(m[2][2]);
        buffer.flip();
    }

    //endregion
}
