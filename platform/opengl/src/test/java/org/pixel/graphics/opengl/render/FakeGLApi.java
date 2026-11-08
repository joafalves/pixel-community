package org.pixel.graphics.opengl.render;

import org.pixel.graphics.opengl.GLApi;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Records the GL calls the renderer makes (no GPU). With {@code capture} off it only counts, without allocating,
 * so it can be used in allocation tests.
 */
final class FakeGLApi implements GLApi {

    record Draw(int program, int vertexArray, int count, long offset, int framebuffer) {
    }

    record Upload(int target, int buffer, int offset, int size) {
    }

    record Range(int index, int buffer, long offset, long size) {
    }

    final boolean capture;
    final List<Draw> draws = new ArrayList<>();
    final List<Upload> uploads = new ArrayList<>();
    final List<Range> ranges = new ArrayList<>();
    final List<String> shaderSources = new ArrayList<>();
    int drawCount;
    int textureBinds;
    private int nextName = 1;
    private int program;
    private int vertexArray;
    private int arrayBuffer;

    FakeGLApi(boolean capture) {
        this.capture = capture;
    }

    void clear() {
        draws.clear();
        uploads.clear();
        ranges.clear();
        drawCount = 0;
        textureBinds = 0;
        calls.clear();
    }

    @Override
    public String shaderPreamble() {
        return "#version 300 es\n";
    }

    @Override
    public int getInteger(int name) {
        return switch (name) {
            case GL_MAX_TEXTURE_IMAGE_UNITS -> 16;
            case GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT -> 256;
            case GL_FRAMEBUFFER_BINDING -> framebuffer;
            default -> 0;
        };
    }

    @Override
    public void blendFunc(int sourceFactor, int destinationFactor) {
    }

    final List<String> calls = new ArrayList<>(); // framebuffer, viewport and post calls (when capturing)
    int framebuffer;
    final int[] viewportState = {0, 0, 800, 600};

    private void log(String call) {
        if (capture) {
            calls.add(call);
        }
    }

    @Override
    public void getIntegerv(int name, int[] out) {
        if (name == GL_VIEWPORT) {
            System.arraycopy(viewportState, 0, out, 0, 4);
        }
    }

    @Override
    public void blendFuncSeparate(int sourceRgb, int destinationRgb, int sourceAlpha, int destinationAlpha) {
    }

    @Override
    public void enable(int capability) {
    }

    @Override
    public void disable(int capability) {
    }

    @Override
    public void viewport(int x, int y, int width, int height) {
        viewportState[0] = x;
        viewportState[1] = y;
        viewportState[2] = width;
        viewportState[3] = height;
        log("viewport " + width + "x" + height);
    }

    @Override
    public void clearColor(float r, float g, float b, float a) {
    }

    @Override
    public void clear(int mask) {
        log("clear fb" + framebuffer);
    }

    @Override
    public void readPixels(int x, int y, int width, int height, ByteBuffer pixels) {
        log("read fb" + framebuffer);
    }

    @Override
    public int genFramebuffer() {
        return nextName++;
    }

    @Override
    public void deleteFramebuffer(int framebuffer) {
    }

    @Override
    public void bindFramebuffer(int target, int framebuffer) {
        this.framebuffer = framebuffer;
        log("bind fb" + framebuffer);
    }

    @Override
    public void framebufferTexture2D(int target, int attachment, int textureTarget, int texture, int level) {
    }

    @Override
    public int checkFramebufferStatus(int target) {
        return GL_FRAMEBUFFER_COMPLETE;
    }

    @Override
    public void drawArrays(int mode, int first, int count) {
        drawCount++;
        log("drawArrays fb" + framebuffer);
    }

    @Override
    public void uniform1i(int location, int value) {
    }

    @Override
    public void uniform1f(int location, float x) {
    }

    @Override
    public void uniform2f(int location, float x, float y) {
    }

    @Override
    public void uniform4f(int location, float x, float y, float z, float w) {
    }

    @Override
    public int genBuffer() {
        return nextName++;
    }

    @Override
    public void deleteBuffer(int buffer) {
    }

    @Override
    public void bindBuffer(int target, int buffer) {
        if (target == GL_ARRAY_BUFFER) {
            arrayBuffer = buffer;
        }
    }

    @Override
    public void bufferData(int target, long size, int usage) {
    }

    @Override
    public void bufferData(int target, ShortBuffer data, int usage) {
    }

    @Override
    public void bufferData(int target, ByteBuffer data, int offset, int size, int usage) {
        if (offset < 0 || offset + size > data.capacity()) {
            throw new IndexOutOfBoundsException();
        }
        if (capture) {
            uploads.add(new Upload(target, target == GL_ARRAY_BUFFER ? arrayBuffer : 0, offset, size));
        }
    }

    @Override
    public void bufferSubData(int target, long offset, FloatBuffer data) {
    }

    @Override
    public void bindBufferRange(int target, int index, int buffer, long offset, long size) {
        if (capture) {
            ranges.add(new Range(index, buffer, offset, size));
        }
    }

    @Override
    public int genVertexArray() {
        return nextName++;
    }

    @Override
    public void deleteVertexArray(int array) {
    }

    @Override
    public void bindVertexArray(int array) {
        vertexArray = array;
    }

    @Override
    public void enableVertexAttribArray(int index) {
    }

    @Override
    public void vertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long offset) {
    }

    @Override
    public void vertexAttribIPointer(int index, int size, int type, int stride, long offset) {
    }

    @Override
    public void drawElements(int mode, int count, int type, long offset) {
        drawCount++;
        if (capture) {
            draws.add(new Draw(program, vertexArray, count, offset, framebuffer));
        }
    }

    @Override
    public int genTexture() {
        return nextName++;
    }

    @Override
    public void deleteTexture(int texture) {
    }

    @Override
    public void activeTexture(int unit) {
    }

    @Override
    public void bindTexture(int target, int texture) {
        textureBinds++;
    }

    @Override
    public void texParameteri(int target, int name, int value) {
    }

    @Override
    public void texImage2D(int target, int level, int internalFormat, int width, int height, int format, int type,
                           ByteBuffer pixels) {
    }

    @Override
    public int createShader(int type) {
        return nextName++;
    }

    @Override
    public void shaderSource(int shader, String source) {
        shaderSources.add(source);
    }

    @Override
    public void compileShader(int shader) {
    }

    @Override
    public int getShaderi(int shader, int name) {
        return GL_TRUE;
    }

    @Override
    public String getShaderInfoLog(int shader) {
        return "";
    }

    @Override
    public void deleteShader(int shader) {
    }

    @Override
    public int createProgram() {
        return nextName++;
    }

    @Override
    public void attachShader(int program, int shader) {
    }

    @Override
    public void linkProgram(int program) {
    }

    @Override
    public int getProgrami(int program, int name) {
        return GL_TRUE;
    }

    @Override
    public String getProgramInfoLog(int program) {
        return "";
    }

    @Override
    public void deleteProgram(int program) {
    }

    @Override
    public void useProgram(int program) {
        this.program = program;
    }

    @Override
    public int getUniformLocation(int program, String name) {
        return 0;
    }

    @Override
    public int getUniformBlockIndex(int program, String name) {
        return 0;
    }

    @Override
    public void uniformBlockBinding(int program, int blockIndex, int binding) {
    }

    @Override
    public void uniform1iv(int location, int[] values) {
    }

    @Override
    public void uniformMatrix4fv(int location, FloatBuffer matrix) {
    }
}
