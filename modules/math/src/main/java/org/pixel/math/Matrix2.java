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
     * Constructor. Matrix with identity values.
     */
    public Matrix2() {
        setIdentity();
    }

    /**
     * Constructor.
     * @param other The matrix to copy values from.
     */
    public Matrix2(Matrix2 other) {
        this.m[0][0] = other.m[0][0];
        this.m[0][1] = other.m[0][1];
        this.m[1][0] = other.m[1][0];
        this.m[1][1] = other.m[1][1];
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
     * Sets the values to an identity matrix.
     */
    public void setIdentity() {
        m[0][0] = 1.0f;
        m[1][1] = 1.0f;
        m[0][1] = 0.0f;
        m[1][0] = 0.0f;
    }

    /**
     * Matrix addition.
     *
     * @param matrix The matrix to add.
     */
    public void add(Matrix2 matrix) {
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[0].length; j++) {
                m[i][j] += matrix.m[i][j];
            }
        }
    }

    /**
     * Matrix subtraction.
     *
     * @param matrix The matrix to subtract.
     */
    public void subtract(Matrix2 matrix) {
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[0].length; j++) {
                m[i][j] -= matrix.m[i][j];
            }
        }
    }

    /**
     * Matrix multiplication.
     *
     * @param matrix The matrix to multiply by.
     */
    public void multiply(Matrix2 matrix) {
        tmp[0][0] = m[0][0] * matrix.m[0][0] + m[0][1] * matrix.m[1][0];
        tmp[1][0] = m[1][0] * matrix.m[0][0] + m[1][1] * matrix.m[1][0];

        tmp[0][1] = m[0][0] * matrix.m[0][1] + m[0][1] * matrix.m[1][1];
        tmp[1][1] = m[1][0] * matrix.m[0][1] + m[1][1] * matrix.m[1][1];

        assignFromTmp();
    }

    /**
     * Matrix multiplication by a scalar.
     *
     * @param scalar The scalar to multiply by.
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
        float o10 = m[1][0];
        m[1][0] = m[0][1];
        m[0][1] = o10;
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
        buffer.put(m[0][0]).put(m[1][0]);
        buffer.put(m[0][1]).put(m[1][1]);
        buffer.flip();
    }

    //endregion
}
