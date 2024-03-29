/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.math;

import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class Matrix4 implements Serializable {

    //region properties

    private final float[][] m = new float[4][4];
    private transient final float[][] tmp = new float[4][4];

    //endregion

    //region constructors

    /**
     * Constructor. Matrix with identity values.
     */
    public Matrix4() {
        setIdentity();
    }

    /**
     * Constructor.
     *
     * @param other The matrix to copy values from.
     */
    public Matrix4(Matrix4 other) {
        this.m[0][0] = other.m[0][0];
        this.m[0][1] = other.m[0][1];
        this.m[0][2] = other.m[0][2];
        this.m[0][3] = other.m[0][3];
        this.m[1][0] = other.m[1][0];
        this.m[1][1] = other.m[1][1];
        this.m[1][2] = other.m[1][2];
        this.m[1][3] = other.m[1][3];
        this.m[2][0] = other.m[2][0];
        this.m[2][1] = other.m[2][1];
        this.m[2][2] = other.m[2][2];
        this.m[2][3] = other.m[2][3];
        this.m[3][0] = other.m[3][0];
        this.m[3][1] = other.m[3][1];
        this.m[3][2] = other.m[3][2];
        this.m[3][3] = other.m[3][3];
    }

    //endregion

    //region public static methods

    /**
     * Creates an orthographic projection matrix.
     *
     * @param left   The coordinate for the left vertical clipping pane.
     * @param right  The coordinate for the right vertical clipping pane.
     * @param bottom The coordinate for the bottom horizontal clipping pane.
     * @param top    The coordinate for the top horizontal clipping pane.
     * @param near   The coordinate for the near depth clipping pane.
     * @param far    The coordinate for the far depth clipping pane.
     * @return The orthographic projection matrix.
     */
    public static Matrix4 orthographic(float left, float right, float bottom, float top, float near, float far) {
        Matrix4 matrix = new Matrix4();
        matrix.setOrthographic(left, right, bottom, top, near, far);
        return matrix;
    }

    /**
     * Creates a perspective projection matrix.
     *
     * @param left   The coordinate for the left vertical clipping pane.
     * @param right  The coordinate for the right vertical clipping pane.
     * @param bottom The coordinate for the bottom horizontal clipping pane.
     * @param top    The coordinate for the top horizontal clipping pane.
     * @param near   The coordinate for the near depth clipping pane.
     * @param far    The coordinate for the far depth clipping pane.
     * @return The perspective projection matrix.
     */
    public static Matrix4 frustum(float left, float right, float bottom, float top, float near, float far) {
        Matrix4 matrix = new Matrix4();
        matrix.m[0][0] = (2.0f * near) / (right - left);
        matrix.m[1][1] = (2.0f * near) / (top - bottom);
        matrix.m[0][2] = (right + left) / (right - left);
        matrix.m[1][2] = (top + bottom) / (top - bottom);
        matrix.m[2][2] = -(far + near) / (far - near);
        matrix.m[3][2] = -1.0f;
        matrix.m[2][3] = -(2.0f * far * near) / (far - near);
        matrix.m[3][3] = 0.0f;

        return matrix;
    }

    /**
     * Creates a perspective projection matrix.
     *
     * @param fovy   The field of view angle, in radians.
     * @param aspect The aspect ratio (width to height).
     * @param near   The distance for the near depth clipping pane.
     * @param far    The distance for the far depth clipping pane.
     * @return The perspective projection matrix.
     */
    public static Matrix4 perspective(float fovy, float aspect, float near, float far) {
        float o = (float) (1.0f / Math.tan(fovy / 2.0f));
        Matrix4 matrix = new Matrix4();
        matrix.m[0][0] = o / aspect;
        matrix.m[1][1] = o;
        matrix.m[2][2] = (far + near) / (near - far);
        matrix.m[3][2] = -1.0f;
        matrix.m[2][3] = (2.0f * far * near) / (near - far);
        matrix.m[3][3] = 0f;

        return matrix;
    }

    //endregion

    //region private methods

    private void assignFromTmp() {
        m[0][0] = tmp[0][0];
        m[1][0] = tmp[1][0];
        m[2][0] = tmp[2][0];
        m[3][0] = tmp[3][0];

        m[0][1] = tmp[0][1];
        m[1][1] = tmp[1][1];
        m[2][1] = tmp[2][1];
        m[3][1] = tmp[3][1];

        m[0][2] = tmp[0][2];
        m[1][2] = tmp[1][2];
        m[2][2] = tmp[2][2];
        m[3][2] = tmp[3][2];

        m[0][3] = tmp[0][3];
        m[1][3] = tmp[1][3];
        m[2][3] = tmp[2][3];
        m[3][3] = tmp[3][3];
    }

    //endregion

    //region public methods

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Matrix4)) {
            return false;
        }

        var other = (Matrix4) obj;
        for (var i = 0; i < m.length; i++) {
            if (!Arrays.equals(m[i], other.m[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Clears all matrix values (sets all values to zero).
     */
    public void clear() {
        m[0][0] = 0.0f;
        m[0][1] = 0.0f;
        m[0][2] = 0.0f;
        m[0][3] = 0.0f;
        m[1][0] = 0.0f;
        m[1][1] = 0.0f;
        m[1][2] = 0.0f;
        m[1][3] = 0.0f;
        m[2][0] = 0.0f;
        m[2][1] = 0.0f;
        m[2][2] = 0.0f;
        m[2][3] = 0.0f;
        m[3][0] = 0.0f;
        m[3][1] = 0.0f;
        m[3][2] = 0.0f;
        m[3][3] = 0.0f;
    }

    /**
     * Set the values to an orthographic projection matrix.
     *
     * @param left   The coordinate for the left vertical clipping pane.
     * @param right  The coordinate for the right vertical clipping pane.
     * @param bottom The coordinate for the bottom horizontal clipping pane.
     * @param top    The coordinate for the top horizontal clipping pane.
     * @param near   The coordinate for the near depth clipping pane.
     * @param far    The coordinate for the far depth clipping pane.
     */
    public void setOrthographic(float left, float right, float bottom, float top, float near, float far) {
        setIdentity();

        m[0][0] = 2.0f / (right - left);
        m[1][1] = 2.0f / (top - bottom);
        m[2][2] = 1 / (far - near);
        m[3][0] = (left + right) / (left - right);
        m[3][1] = (bottom + top) / (bottom - top);
        m[3][2] = near / (near - far);
        m[3][3] = 1.0f;
    }

    /**
     * Sets the values to an identity matrix
     */
    public void setIdentity() {
        m[0][0] = 1.0f;
        m[1][1] = 1.0f;
        m[2][2] = 1.0f;
        m[3][3] = 1.0f;

        m[0][1] = 0.0f;
        m[0][2] = 0.0f;
        m[0][3] = 0.0f;
        m[1][0] = 0.0f;
        m[1][2] = 0.0f;
        m[1][3] = 0.0f;
        m[2][0] = 0.0f;
        m[2][1] = 0.0f;
        m[2][3] = 0.0f;
        m[3][0] = 0.0f;
        m[3][1] = 0.0f;
        m[3][2] = 0.0f;
    }

    /**
     * Matrix addition.
     *
     * @param matrix The matrix to add.
     */
    public void add(Matrix4 matrix) {
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
    public void subtract(Matrix4 matrix) {
        for (int i = 0; i < m.length; ++i) {
            for (int j = 0; j < m[0].length; ++j) {
                m[i][j] -= matrix.m[i][j];
            }
        }
    }

    /**
     * Matrix multiplication.
     *
     * @param matrix The matrix to multiply.
     */
    public void multiply(Matrix4 matrix) {
        tmp[0][0] = m[0][0] * matrix.m[0][0] + m[0][1] * matrix.m[1][0] + m[0][2] * matrix.m[2][0] + m[0][3] * m[3][0];
        tmp[1][0] = m[1][0] * matrix.m[0][0] + m[1][1] * matrix.m[1][0] + m[1][2] * matrix.m[2][0] + m[1][3] * m[3][0];
        tmp[2][0] = m[2][0] * matrix.m[0][0] + m[2][1] * matrix.m[1][0] + m[2][2] * matrix.m[2][0] + m[2][3] * m[3][0];
        tmp[3][0] = m[3][0] * matrix.m[0][0] + m[3][1] * matrix.m[1][0] + m[3][2] * matrix.m[2][0] + m[3][3] * m[3][0];

        tmp[0][1] = m[0][0] * matrix.m[0][1] + m[0][1] * matrix.m[1][1] + m[0][2] * matrix.m[2][1] + m[0][3] * m[3][1];
        tmp[1][1] = m[1][0] * matrix.m[0][1] + m[1][1] * matrix.m[1][1] + m[1][2] * matrix.m[2][1] + m[1][3] * m[3][1];
        tmp[2][1] = m[2][0] * matrix.m[0][1] + m[2][1] * matrix.m[1][1] + m[2][2] * matrix.m[2][1] + m[2][3] * m[3][1];
        tmp[3][1] = m[3][0] * matrix.m[0][1] + m[3][1] * matrix.m[1][1] + m[3][2] * matrix.m[2][1] + m[3][3] * m[3][1];

        tmp[0][2] = m[0][0] * matrix.m[0][2] + m[0][1] * matrix.m[1][2] + m[0][2] * matrix.m[2][2] + m[0][3] * m[3][2];
        tmp[1][2] = m[1][0] * matrix.m[0][2] + m[1][1] * matrix.m[1][2] + m[1][2] * matrix.m[2][2] + m[1][3] * m[3][2];
        tmp[2][2] = m[2][0] * matrix.m[0][2] + m[2][1] * matrix.m[1][2] + m[2][2] * matrix.m[2][2] + m[2][3] * m[3][2];
        tmp[3][2] = m[3][0] * matrix.m[0][2] + m[3][1] * matrix.m[1][2] + m[3][2] * matrix.m[2][2] + m[3][3] * m[3][2];

        tmp[0][3] = m[0][0] * matrix.m[0][3] + m[0][1] * matrix.m[1][3] + m[0][2] * matrix.m[2][3] + m[0][3] * m[3][3];
        tmp[1][3] = m[1][0] * matrix.m[0][3] + m[1][1] * matrix.m[1][3] + m[1][2] * matrix.m[2][3] + m[1][3] * m[3][3];
        tmp[2][3] = m[2][0] * matrix.m[0][3] + m[2][1] * matrix.m[1][3] + m[2][2] * matrix.m[2][3] + m[2][3] * m[3][3];
        tmp[3][3] = m[3][0] * matrix.m[0][3] + m[3][1] * matrix.m[1][3] + m[3][2] * matrix.m[2][3] + m[3][3] * m[3][3];

        assignFromTmp();
    }

    /**
     * Matrix multiplication by a scalar.
     *
     * @param scalar The scalar.
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
     * Inverts the matrix.
     */
    public void invert() {
        float num1 = m[0][0];
        float num2 = m[0][1];
        float num3 = m[0][2];
        float num4 = m[0][3];
        float num5 = m[1][0];
        float num6 = m[1][1];
        float num7 = m[1][2];
        float num8 = m[1][3];
        float num9 = m[2][0];
        float num10 = m[2][1];
        float num11 = m[2][2];
        float num12 = m[2][3];
        float num13 = m[3][0];
        float num14 = m[3][1];
        float num15 = m[3][2];
        float num16 = m[3][3];
        float num17 = (float) ((double) num11 * (double) num16 - (double) num12 * (double) num15);
        float num18 = (float) ((double) num10 * (double) num16 - (double) num12 * (double) num14);
        float num19 = (float) ((double) num10 * (double) num15 - (double) num11 * (double) num14);
        float num20 = (float) ((double) num9 * (double) num16 - (double) num12 * (double) num13);
        float num21 = (float) ((double) num9 * (double) num15 - (double) num11 * (double) num13);
        float num22 = (float) ((double) num9 * (double) num14 - (double) num10 * (double) num13);
        float num23 = (float) ((double) num6 * (double) num17 - (double) num7 * (double) num18
                + (double) num8 * (double) num19);
        float num24 = (float) -((double) num5 * (double) num17 - (double) num7 * (double) num20
                + (double) num8 * (double) num21);
        float num25 = (float) ((double) num5 * (double) num18 - (double) num6 * (double) num20
                + (double) num8 * (double) num22);
        float num26 = (float) -((double) num5 * (double) num19 - (double) num6 * (double) num21
                + (double) num7 * (double) num22);
        float num27 = (float) (1.0 / ((double) num1 * (double) num23 + (double) num2 * (double) num24
                + (double) num3 * (double) num25 + (double) num4 * (double) num26));
        float num28 = (float) ((double) num7 * (double) num16 - (double) num8 * (double) num15);
        float num29 = (float) ((double) num6 * (double) num16 - (double) num8 * (double) num14);
        float num30 = (float) ((double) num6 * (double) num15 - (double) num7 * (double) num14);
        float num31 = (float) ((double) num5 * (double) num16 - (double) num8 * (double) num13);
        float num32 = (float) ((double) num5 * (double) num15 - (double) num7 * (double) num13);
        float num33 = (float) ((double) num5 * (double) num14 - (double) num6 * (double) num13);
        float num34 = (float) ((double) num7 * (double) num12 - (double) num8 * (double) num11);
        float num35 = (float) ((double) num6 * (double) num12 - (double) num8 * (double) num10);
        float num36 = (float) ((double) num6 * (double) num11 - (double) num7 * (double) num10);
        float num37 = (float) ((double) num5 * (double) num12 - (double) num8 * (double) num9);
        float num38 = (float) ((double) num5 * (double) num11 - (double) num7 * (double) num9);
        float num39 = (float) ((double) num5 * (double) num10 - (double) num6 * (double) num9);

        m[0][0] = num23 * num27;
        m[1][0] = num24 * num27;
        m[2][0] = num25 * num27;
        m[3][0] = num26 * num27;
        m[0][1] = (float) -((double) num2 * (double) num17 - (double) num3 * (double) num18
                + (double) num4 * (double) num19) * num27;
        m[1][1] = (float) ((double) num1 * (double) num17 - (double) num3 * (double) num20
                + (double) num4 * (double) num21) * num27;
        m[2][1] = (float) -((double) num1 * (double) num18 - (double) num2 * (double) num20
                + (double) num4 * (double) num22) * num27;
        m[3][1] = (float) ((double) num1 * (double) num19 - (double) num2 * (double) num21
                + (double) num3 * (double) num22) * num27;
        m[0][2] = (float) ((double) num2 * (double) num28 - (double) num3 * (double) num29
                + (double) num4 * (double) num30) * num27;
        m[1][2] = (float) -((double) num1 * (double) num28 - (double) num3 * (double) num31
                + (double) num4 * (double) num32) * num27;
        m[2][2] = (float) ((double) num1 * (double) num29 - (double) num2 * (double) num31
                + (double) num4 * (double) num33) * num27;
        m[3][2] = (float) -((double) num1 * (double) num30 - (double) num2 * (double) num32
                + (double) num3 * (double) num33) * num27;
        m[0][3] = (float) -((double) num2 * (double) num34 - (double) num3 * (double) num35
                + (double) num4 * (double) num36) * num27;
        m[1][3] = (float) ((double) num1 * (double) num34 - (double) num3 * (double) num37
                + (double) num4 * (double) num38) * num27;
        m[2][3] = (float) -((double) num1 * (double) num35 - (double) num2 * (double) num37
                + (double) num4 * (double) num39) * num27;
        m[3][3] = (float) ((double) num1 * (double) num36 - (double) num2 * (double) num38
                + (double) num3 * (double) num39) * num27;
    }

    /**
     * Transposes the matrix.
     */
    public void transpose() {
        tmp[0][0] = m[0][0];
        tmp[1][0] = m[0][1];
        tmp[2][0] = m[0][2];
        tmp[3][0] = m[0][3];

        tmp[0][1] = m[1][0];
        tmp[1][1] = m[1][1];
        tmp[2][1] = m[1][2];
        tmp[3][1] = m[1][3];

        tmp[0][2] = m[2][0];
        tmp[1][2] = m[2][1];
        tmp[2][2] = m[2][2];
        tmp[3][2] = m[2][3];

        tmp[0][3] = m[3][0];
        tmp[1][3] = m[3][1];
        tmp[2][3] = m[3][2];
        tmp[3][3] = m[3][3];

        assignFromTmp();
    }

    /**
     * Translates matrix by a given vector.
     *
     * @param position The vector to translate by.
     */
    public void translate(Vector3 position) {
        this.translate(position.getX(), position.getY(), position.getZ());
    }

    /**
     * Translates matrix by the given coordinates.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     */
    public void translate(float x, float y, float z) {
        m[3][0] = m[0][0] * x + m[1][0] * y + m[2][0] * z + m[3][0];
        m[3][1] = m[0][1] * x + m[1][1] * y + m[2][1] * z + m[3][1];
        m[3][2] = m[0][2] * x + m[1][2] * y + m[2][2] * z + m[3][2];
        m[3][3] = m[0][3] * x + m[1][3] * y + m[2][3] * z + m[3][3];
    }

    /**
     * Rotates the matrix along an axis.
     *
     * @param angle The angle to rotate by.
     * @param axis  The axis to rotate along.
     */
    public void rotate(float angle, Vector3 axis) {
        // axis.normalize(); // should do this to prevent issues... but Math.sqrt is quite costly...
        this.rotate(angle, axis.getX(), axis.getY(), axis.getZ());
    }

    /**
     * Rotates the matrix along an axis.
     *
     * @param angle The angle to rotate by in radians.
     * @param x     The x coordinate magnitude (from 0 to 1)
     * @param y     The y coordinate magnitude (from 0 to 1)
     * @param z     The z coordinate magnitude (from 0 to 1)
     */
    public void rotate(float angle, float x, float y, float z) {
        float c = MathHelper.cos(angle);
        float s = MathHelper.sin(-angle); // y-axis is inverted in Pixel

        m[0][0] = x * x * (1f - c) + c;
        m[1][0] = y * x * (1f - c) + z * s;
        m[2][0] = x * z * (1f - c) - y * s;
        m[0][1] = x * y * (1f - c) - z * s;
        m[1][1] = y * y * (1f - c) + c;
        m[2][1] = y * z * (1f - c) + x * s;
        m[0][2] = x * z * (1f - c) + y * s;
        m[1][2] = y * z * (1f - c) - x * s;
        m[2][2] = z * z * (1f - c) + c;
    }

    /**
     * Scales the matrix by the given vector.
     *
     * @param factor The vector to scale by.
     */
    public void scale(Vector3 factor) {
        this.scale(factor.getX(), factor.getY(), factor.getZ());
    }

    /**
     * Scales the matrix by coordinates scaling values.
     *
     * @param x The x coordinate scaling value.
     * @param y The y coordinate scaling value.
     * @param z The z coordinate scaling value.
     */
    public void scale(float x, float y, float z) {
        m[0][0] *= x;
        m[0][1] *= x;
        m[0][2] *= x;
        m[0][3] *= x;
        m[1][0] *= y;
        m[1][1] *= y;
        m[1][2] *= y;
        m[1][3] *= y;
        m[2][0] *= z;
        m[2][1] *= z;
        m[2][2] *= z;
        m[2][3] *= z;
    }

    /**
     * Clones and returns the array value of the matrix.
     *
     * @return The array value of the matrix.
     */
    public float[][] toArray() {
        return m.clone();
    }

    /**
     * Returns the original array value of the matrix.
     *
     * @return The original array value of the matrix.
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
        buffer.put(m[0][0]).put(m[0][1]).put(m[0][2]).put(m[0][3]);
        buffer.put(m[1][0]).put(m[1][1]).put(m[1][2]).put(m[1][3]);
        buffer.put(m[2][0]).put(m[2][1]).put(m[2][2]).put(m[2][3]);
        buffer.put(m[3][0]).put(m[3][1]).put(m[3][2]).put(m[3][3]);
        buffer.flip();
    }

    //endregion
}
