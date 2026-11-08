package org.pixel.graphics.opengl;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL30C.*;
import static org.lwjgl.opengl.GL31C.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31C.glUniformBlockBinding;
import static org.lwjgl.opengl.GL14C.glBlendFuncSeparate;

/**
 * {@link GLApi} on desktop OpenGL 3.3 core through LWJGL. Stateless: use {@link #INSTANCE}.
 */
public final class LwjglGLApi implements GLApi {

    public static final LwjglGLApi INSTANCE = new LwjglGLApi();

    private static final String PREAMBLE = "#version 330 core\nprecision highp float;\nprecision highp int;\n";

    private LwjglGLApi() {
    }

    @Override
    public String shaderPreamble() {
        return PREAMBLE;
    }

    @Override
    public int getInteger(int name) {
        return glGetInteger(name);
    }

    @Override
    public void getIntegerv(int name, int[] out) {
        glGetIntegerv(name, out);
    }

    @Override
    public void blendFunc(int sourceFactor, int destinationFactor) {
        glBlendFunc(sourceFactor, destinationFactor);
    }

    @Override
    public void blendFuncSeparate(int sourceRgb, int destinationRgb, int sourceAlpha, int destinationAlpha) {
        glBlendFuncSeparate(sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
    }

    @Override
    public void enable(int capability) {
        glEnable(capability);
    }

    @Override
    public void disable(int capability) {
        glDisable(capability);
    }

    @Override
    public void viewport(int x, int y, int width, int height) {
        glViewport(x, y, width, height);
    }

    @Override
    public void clearColor(float r, float g, float b, float a) {
        glClearColor(r, g, b, a);
    }

    @Override
    public void clear(int mask) {
        glClear(mask);
    }

    @Override
    public void readPixels(int x, int y, int width, int height, ByteBuffer pixels) {
        glReadPixels(x, y, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
    }

    // region framebuffers

    @Override
    public int genFramebuffer() {
        return glGenFramebuffers();
    }

    @Override
    public void deleteFramebuffer(int framebuffer) {
        glDeleteFramebuffers(framebuffer);
    }

    @Override
    public void bindFramebuffer(int target, int framebuffer) {
        glBindFramebuffer(target, framebuffer);
    }

    @Override
    public void framebufferTexture2D(int target, int attachment, int textureTarget, int texture, int level) {
        glFramebufferTexture2D(target, attachment, textureTarget, texture, level);
    }

    @Override
    public int checkFramebufferStatus(int target) {
        return glCheckFramebufferStatus(target);
    }

    @Override
    public void drawArrays(int mode, int first, int count) {
        glDrawArrays(mode, first, count);
    }

    // endregion

    // region buffers

    @Override
    public int genBuffer() {
        return glGenBuffers();
    }

    @Override
    public void deleteBuffer(int buffer) {
        glDeleteBuffers(buffer);
    }

    @Override
    public void bindBuffer(int target, int buffer) {
        glBindBuffer(target, buffer);
    }

    @Override
    public void bufferData(int target, long size, int usage) {
        glBufferData(target, size, usage);
    }

    @Override
    public void bufferData(int target, ShortBuffer data, int usage) {
        glBufferData(target, data, usage);
    }

    @Override
    public void bufferData(int target, ByteBuffer data, int offset, int size, int usage) {
        if (offset < 0 || size < 0 || offset + size > data.capacity()) {
            throw new IndexOutOfBoundsException("Range " + offset + "+" + size + " outside a " + data.capacity()
                    + " byte buffer.");
        }
        nglBufferData(target, size, MemoryUtil.memAddress0(data) + offset, usage); // by address: no views
    }

    @Override
    public void bufferSubData(int target, long offset, FloatBuffer data) {
        glBufferSubData(target, offset, data);
    }

    @Override
    public void bindBufferRange(int target, int index, int buffer, long offset, long size) {
        glBindBufferRange(target, index, buffer, offset, size);
    }

    // endregion

    // region vertex arrays and drawing

    @Override
    public int genVertexArray() {
        return glGenVertexArrays();
    }

    @Override
    public void deleteVertexArray(int array) {
        glDeleteVertexArrays(array);
    }

    @Override
    public void bindVertexArray(int array) {
        glBindVertexArray(array);
    }

    @Override
    public void enableVertexAttribArray(int index) {
        glEnableVertexAttribArray(index);
    }

    @Override
    public void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long offset) {
        glVertexAttribPointer(index, size, type, normalized, stride, offset);
    }

    @Override
    public void vertexAttribIPointer(int index, int size, int type, int stride, long offset) {
        glVertexAttribIPointer(index, size, type, stride, offset);
    }

    @Override
    public void drawElements(int mode, int count, int type, long offset) {
        glDrawElements(mode, count, type, offset);
    }

    // endregion

    // region textures

    @Override
    public int genTexture() {
        return glGenTextures();
    }

    @Override
    public void deleteTexture(int texture) {
        glDeleteTextures(texture);
    }

    @Override
    public void activeTexture(int unit) {
        glActiveTexture(unit);
    }

    @Override
    public void bindTexture(int target, int texture) {
        glBindTexture(target, texture);
    }

    @Override
    public void texParameteri(int target, int name, int value) {
        glTexParameteri(target, name, value);
    }

    @Override
    public void texImage2D(int target, int level, int internalFormat, int width, int height, int format, int type,
                           ByteBuffer pixels) {
        glTexImage2D(target, level, internalFormat, width, height, 0, format, type, pixels);
    }

    // endregion

    // region programs

    @Override
    public int createShader(int type) {
        return glCreateShader(type);
    }

    @Override
    public void shaderSource(int shader, String source) {
        glShaderSource(shader, source);
    }

    @Override
    public void compileShader(int shader) {
        glCompileShader(shader);
    }

    @Override
    public int getShaderi(int shader, int name) {
        return glGetShaderi(shader, name);
    }

    @Override
    public String getShaderInfoLog(int shader) {
        return glGetShaderInfoLog(shader);
    }

    @Override
    public void deleteShader(int shader) {
        glDeleteShader(shader);
    }

    @Override
    public int createProgram() {
        return glCreateProgram();
    }

    @Override
    public void attachShader(int program, int shader) {
        glAttachShader(program, shader);
    }

    @Override
    public void linkProgram(int program) {
        glLinkProgram(program);
    }

    @Override
    public int getProgrami(int program, int name) {
        return glGetProgrami(program, name);
    }

    @Override
    public String getProgramInfoLog(int program) {
        return glGetProgramInfoLog(program);
    }

    @Override
    public void deleteProgram(int program) {
        glDeleteProgram(program);
    }

    @Override
    public void useProgram(int program) {
        glUseProgram(program);
    }

    @Override
    public int getUniformLocation(int program, String name) {
        return glGetUniformLocation(program, name);
    }

    @Override
    public int getUniformBlockIndex(int program, String name) {
        return glGetUniformBlockIndex(program, name);
    }

    @Override
    public void uniformBlockBinding(int program, int blockIndex, int binding) {
        glUniformBlockBinding(program, blockIndex, binding);
    }

    @Override
    public void uniform1iv(int location, int[] values) {
        glUniform1iv(location, values);
    }

    @Override
    public void uniform1i(int location, int value) {
        glUniform1i(location, value);
    }

    @Override
    public void uniform1f(int location, float x) {
        glUniform1f(location, x);
    }

    @Override
    public void uniform2f(int location, float x, float y) {
        glUniform2f(location, x, y);
    }

    @Override
    public void uniform4f(int location, float x, float y, float z, float w) {
        glUniform4f(location, x, y, z, w);
    }

    @Override
    public void uniformMatrix4fv(int location, FloatBuffer matrix) {
        glUniformMatrix4fv(location, false, matrix);
    }

    // endregion
}
