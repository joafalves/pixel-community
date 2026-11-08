package org.pixel.graphics.opengl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * The OpenGL functions the renderer uses, implemented per platform: OpenGL 3.3 core through LWJGL on desktop,
 * OpenGL ES 3.0 on Android. Everything here exists in both APIs with the same enum values, so the shared renderer
 * code (and its GLSL, apart from {@link #shaderPreamble()}) is identical on every platform.
 * <p>
 * Calls must be made on the thread that owns the GL context. Buffers are direct, in native byte order; a buffer
 * argument is read from its position to its limit unless a range is given. Implementations must not allocate per
 * call.
 */
public interface GLApi {

    // region constants (same values in OpenGL and OpenGL ES)

    int GL_FALSE = 0;
    int GL_TRUE = 1;
    int GL_ZERO = 0;
    int GL_ONE = 1;
    int GL_TRIANGLES = 0x0004;
    int GL_SRC_ALPHA = 0x0302;
    int GL_ONE_MINUS_SRC_ALPHA = 0x0303;
    int GL_DST_COLOR = 0x0306;
    int GL_UNSIGNED_BYTE = 0x1401;
    int GL_UNSIGNED_SHORT = 0x1403;
    int GL_FLOAT = 0x1406;
    int GL_RGBA = 0x1908;
    int GL_RGBA8 = 0x8058;
    int GL_NEAREST = 0x2600;
    int GL_LINEAR = 0x2601;
    int GL_TEXTURE_MAG_FILTER = 0x2800;
    int GL_TEXTURE_MIN_FILTER = 0x2801;
    int GL_TEXTURE_WRAP_S = 0x2802;
    int GL_TEXTURE_WRAP_T = 0x2803;
    int GL_TEXTURE_2D = 0x0DE1;
    int GL_CLAMP_TO_EDGE = 0x812F;
    int GL_TEXTURE0 = 0x84C0;
    int GL_ARRAY_BUFFER = 0x8892;
    int GL_ELEMENT_ARRAY_BUFFER = 0x8893;
    int GL_STREAM_DRAW = 0x88E0;
    int GL_STATIC_DRAW = 0x88E4;
    int GL_MAX_TEXTURE_IMAGE_UNITS = 0x8872;
    int GL_FRAGMENT_SHADER = 0x8B30;
    int GL_VERTEX_SHADER = 0x8B31;
    int GL_COMPILE_STATUS = 0x8B81;
    int GL_LINK_STATUS = 0x8B82;
    int GL_UNIFORM_BUFFER = 0x8A11;
    int GL_MAX_UNIFORM_BLOCK_SIZE = 0x8A30;
    int GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT = 0x8A34;
    int GL_INVALID_INDEX = 0xFFFFFFFF;

    // endregion

    /**
     * @return The first lines of every shader: the {@code #version} line and default precisions
     * ({@code #version 330 core} on desktop, {@code #version 300 es} on OpenGL ES).
     */
    String shaderPreamble();

    int getInteger(int name);

    void blendFunc(int sourceFactor, int destinationFactor);

    // region buffers

    int genBuffer();

    void deleteBuffer(int buffer);

    void bindBuffer(int target, int buffer);

    /** Allocate (or orphan) storage of {@code size} bytes without data. */
    void bufferData(int target, long size, int usage);

    /** Allocate storage and fill it with {@code data} (position to limit). */
    void bufferData(int target, ShortBuffer data, int usage);

    /**
     * Allocate storage and fill it with {@code size} bytes of {@code data}, starting at byte {@code offset} from the
     * start of the buffer (its position is ignored and left unchanged).
     */
    void bufferData(int target, ByteBuffer data, int offset, int size, int usage);

    /** Write {@code data} (position to limit) at byte {@code offset} of the bound buffer. */
    void bufferSubData(int target, long offset, FloatBuffer data);

    void bindBufferRange(int target, int index, int buffer, long offset, long size);

    // endregion

    // region vertex arrays and drawing

    int genVertexArray();

    void deleteVertexArray(int array);

    void bindVertexArray(int array);

    void enableVertexAttribArray(int index);

    void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long offset);

    void vertexAttribIPointer(int index, int size, int type, int stride, long offset);

    void drawElements(int mode, int count, int type, long offset);

    // endregion

    // region textures

    int genTexture();

    void deleteTexture(int texture);

    void activeTexture(int unit);

    void bindTexture(int target, int texture);

    void texParameteri(int target, int name, int value);

    /** Upload RGBA8 pixels (rows bottom to top in GL terms; {@code pixels} may be null to allocate only). */
    void texImage2D(int target, int level, int internalFormat, int width, int height, int format, int type,
                    ByteBuffer pixels);

    // endregion

    // region programs

    int createShader(int type);

    void shaderSource(int shader, String source);

    void compileShader(int shader);

    int getShaderi(int shader, int name);

    String getShaderInfoLog(int shader);

    void deleteShader(int shader);

    int createProgram();

    void attachShader(int program, int shader);

    void linkProgram(int program);

    int getProgrami(int program, int name);

    String getProgramInfoLog(int program);

    void deleteProgram(int program);

    void useProgram(int program);

    int getUniformLocation(int program, String name);

    int getUniformBlockIndex(int program, String name);

    void uniformBlockBinding(int program, int blockIndex, int binding);

    void uniform1iv(int location, int[] values);

    /** Set a mat4 uniform from 16 floats (column-major, position to limit). */
    void uniformMatrix4fv(int location, FloatBuffer matrix);

    // endregion
}
