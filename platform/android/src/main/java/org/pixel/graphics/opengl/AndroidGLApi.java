package org.pixel.graphics.opengl;

import android.opengl.GLES30;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * {@link GLApi} on OpenGL ES 3.0 ({@code android.opengl.GLES30}). Use {@link #INSTANCE}, from the GL thread only
 * (a small scratch array is reused between calls).
 */
public final class AndroidGLApi implements GLApi {

    public static final AndroidGLApi INSTANCE = new AndroidGLApi();

    private static final String PREAMBLE = "#version 300 es\nprecision highp float;\nprecision highp int;\n"
            + "precision highp sampler2D;\n";

    private final int[] scratch = new int[1];

    private AndroidGLApi() {
    }

    @Override
    public String shaderPreamble() {
        return PREAMBLE;
    }

    @Override
    public int getInteger(int name) {
        GLES30.glGetIntegerv(name, scratch, 0);
        return scratch[0];
    }

    @Override
    public void getIntegerv(int name, int[] out) {
        GLES30.glGetIntegerv(name, out, 0);
    }

    @Override
    public void blendFunc(int sourceFactor, int destinationFactor) {
        GLES30.glBlendFunc(sourceFactor, destinationFactor);
    }

    @Override
    public void blendFuncSeparate(int sourceRgb, int destinationRgb, int sourceAlpha, int destinationAlpha) {
        GLES30.glBlendFuncSeparate(sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
    }

    @Override
    public void enable(int capability) {
        GLES30.glEnable(capability);
    }

    @Override
    public void disable(int capability) {
        GLES30.glDisable(capability);
    }

    @Override
    public void viewport(int x, int y, int width, int height) {
        GLES30.glViewport(x, y, width, height);
    }

    @Override
    public void clearColor(float r, float g, float b, float a) {
        GLES30.glClearColor(r, g, b, a);
    }

    @Override
    public void clear(int mask) {
        GLES30.glClear(mask);
    }

    @Override
    public void readPixels(int x, int y, int width, int height, ByteBuffer pixels) {
        GLES30.glReadPixels(x, y, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, pixels);
    }

    // region framebuffers

    @Override
    public int genFramebuffer() {
        GLES30.glGenFramebuffers(1, scratch, 0);
        return scratch[0];
    }

    @Override
    public void deleteFramebuffer(int framebuffer) {
        scratch[0] = framebuffer;
        GLES30.glDeleteFramebuffers(1, scratch, 0);
    }

    @Override
    public void bindFramebuffer(int target, int framebuffer) {
        GLES30.glBindFramebuffer(target, framebuffer);
    }

    @Override
    public void framebufferTexture2D(int target, int attachment, int textureTarget, int texture, int level) {
        GLES30.glFramebufferTexture2D(target, attachment, textureTarget, texture, level);
    }

    @Override
    public int checkFramebufferStatus(int target) {
        return GLES30.glCheckFramebufferStatus(target);
    }

    @Override
    public void drawArrays(int mode, int first, int count) {
        GLES30.glDrawArrays(mode, first, count);
    }

    // endregion

    // region buffers

    @Override
    public int genBuffer() {
        GLES30.glGenBuffers(1, scratch, 0);
        return scratch[0];
    }

    @Override
    public void deleteBuffer(int buffer) {
        scratch[0] = buffer;
        GLES30.glDeleteBuffers(1, scratch, 0);
    }

    @Override
    public void bindBuffer(int target, int buffer) {
        GLES30.glBindBuffer(target, buffer);
    }

    @Override
    public void bufferData(int target, long size, int usage) {
        GLES30.glBufferData(target, (int) size, null, usage);
    }

    @Override
    public void bufferData(int target, ShortBuffer data, int usage) {
        GLES30.glBufferData(target, data.remaining() * Short.BYTES, data, usage);
    }

    @Override
    public void bufferData(int target, ByteBuffer data, int offset, int size, int usage) {
        if (offset < 0 || size < 0 || offset + size > data.capacity()) {
            throw new IndexOutOfBoundsException("Range " + offset + "+" + size + " outside a " + data.capacity()
                    + " byte buffer.");
        }
        // GLES reads from the buffer's position: move it to the range, then restore it (no views allocated)
        int position = data.position();
        int limit = data.limit();
        data.limit(data.capacity()).position(offset);
        GLES30.glBufferData(target, size, data, usage);
        data.limit(limit).position(position);
    }

    @Override
    public void bufferSubData(int target, long offset, FloatBuffer data) {
        GLES30.glBufferSubData(target, (int) offset, data.remaining() * Float.BYTES, data);
    }

    @Override
    public void bufferSubData(int target, long offset, ByteBuffer data, int dataOffset, int size) {
        if (dataOffset < 0 || size < 0 || dataOffset + size > data.capacity()) {
            throw new IndexOutOfBoundsException("Range " + dataOffset + "+" + size + " outside a "
                    + data.capacity() + " byte buffer.");
        }
        // GLES reads from the buffer's position: move it to the range, then restore it (no views allocated)
        int position = data.position();
        int limit = data.limit();
        data.limit(data.capacity()).position(dataOffset);
        GLES30.glBufferSubData(target, (int) offset, size, data);
        data.limit(limit).position(position);
    }

    @Override
    public void bindBufferRange(int target, int index, int buffer, long offset, long size) {
        GLES30.glBindBufferRange(target, index, buffer, (int) offset, (int) size);
    }

    // endregion

    // region vertex arrays and drawing

    @Override
    public int genVertexArray() {
        GLES30.glGenVertexArrays(1, scratch, 0);
        return scratch[0];
    }

    @Override
    public void deleteVertexArray(int array) {
        scratch[0] = array;
        GLES30.glDeleteVertexArrays(1, scratch, 0);
    }

    @Override
    public void bindVertexArray(int array) {
        GLES30.glBindVertexArray(array);
    }

    @Override
    public void enableVertexAttribArray(int index) {
        GLES30.glEnableVertexAttribArray(index);
    }

    @Override
    public void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long offset) {
        GLES30.glVertexAttribPointer(index, size, type, normalized, stride, (int) offset);
    }

    @Override
    public void vertexAttribIPointer(int index, int size, int type, int stride, long offset) {
        GLES30.glVertexAttribIPointer(index, size, type, stride, (int) offset);
    }

    @Override
    public void drawElements(int mode, int count, int type, long offset) {
        GLES30.glDrawElements(mode, count, type, (int) offset);
    }

    // endregion

    // region textures

    @Override
    public int genTexture() {
        GLES30.glGenTextures(1, scratch, 0);
        return scratch[0];
    }

    @Override
    public void deleteTexture(int texture) {
        scratch[0] = texture;
        GLES30.glDeleteTextures(1, scratch, 0);
    }

    @Override
    public void activeTexture(int unit) {
        GLES30.glActiveTexture(unit);
    }

    @Override
    public void bindTexture(int target, int texture) {
        GLES30.glBindTexture(target, texture);
    }

    @Override
    public void texParameteri(int target, int name, int value) {
        GLES30.glTexParameteri(target, name, value);
    }

    @Override
    public void texImage2D(int target, int level, int internalFormat, int width, int height, int format, int type,
                           ByteBuffer pixels) {
        GLES30.glTexImage2D(target, level, internalFormat, width, height, 0, format, type, pixels);
    }

    // endregion

    // region programs

    @Override
    public int createShader(int type) {
        return GLES30.glCreateShader(type);
    }

    @Override
    public void shaderSource(int shader, String source) {
        GLES30.glShaderSource(shader, source);
    }

    @Override
    public void compileShader(int shader) {
        GLES30.glCompileShader(shader);
    }

    @Override
    public int getShaderi(int shader, int name) {
        GLES30.glGetShaderiv(shader, name, scratch, 0);
        return scratch[0];
    }

    @Override
    public String getShaderInfoLog(int shader) {
        return GLES30.glGetShaderInfoLog(shader);
    }

    @Override
    public void deleteShader(int shader) {
        GLES30.glDeleteShader(shader);
    }

    @Override
    public int createProgram() {
        return GLES30.glCreateProgram();
    }

    @Override
    public void attachShader(int program, int shader) {
        GLES30.glAttachShader(program, shader);
    }

    @Override
    public void linkProgram(int program) {
        GLES30.glLinkProgram(program);
    }

    @Override
    public int getProgrami(int program, int name) {
        GLES30.glGetProgramiv(program, name, scratch, 0);
        return scratch[0];
    }

    @Override
    public String getProgramInfoLog(int program) {
        return GLES30.glGetProgramInfoLog(program);
    }

    @Override
    public void deleteProgram(int program) {
        GLES30.glDeleteProgram(program);
    }

    @Override
    public void useProgram(int program) {
        GLES30.glUseProgram(program);
    }

    @Override
    public int getUniformLocation(int program, String name) {
        return GLES30.glGetUniformLocation(program, name);
    }

    @Override
    public int getUniformBlockIndex(int program, String name) {
        return GLES30.glGetUniformBlockIndex(program, name);
    }

    @Override
    public void uniformBlockBinding(int program, int blockIndex, int binding) {
        GLES30.glUniformBlockBinding(program, blockIndex, binding);
    }

    @Override
    public void uniform1iv(int location, int[] values) {
        GLES30.glUniform1iv(location, values.length, values, 0);
    }

    @Override
    public void uniform1i(int location, int value) {
        GLES30.glUniform1i(location, value);
    }

    @Override
    public void uniform1f(int location, float x) {
        GLES30.glUniform1f(location, x);
    }

    @Override
    public void uniform2f(int location, float x, float y) {
        GLES30.glUniform2f(location, x, y);
    }

    @Override
    public void uniform4f(int location, float x, float y, float z, float w) {
        GLES30.glUniform4f(location, x, y, z, w);
    }

    @Override
    public void uniformMatrix4fv(int location, FloatBuffer matrix) {
        GLES30.glUniformMatrix4fv(location, 1, false, matrix);
    }

    // endregion
}
