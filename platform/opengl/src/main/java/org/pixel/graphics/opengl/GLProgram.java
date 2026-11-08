package org.pixel.graphics.opengl;

import org.pixel.commons.lifecycle.Disposable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.pixel.graphics.opengl.GLApi.*;

/**
 * A linked vertex + fragment shader program. The platform's {@link GLApi#shaderPreamble()} is prepended to both
 * sources, so shader files contain no {@code #version} line and compile on OpenGL 3.3 and OpenGL ES 3.0.
 */
public class GLProgram implements Disposable {

    protected final GLApi gl;
    private final int program;
    private boolean disposed;

    /**
     * Compile and link (requires a current GL context).
     *
     * @param gl             The GL functions.
     * @param vertexSource   Vertex shader source (without {@code #version}).
     * @param fragmentSource Fragment shader source (without {@code #version}).
     * @throws IllegalStateException With the driver's log if compiling or linking fails.
     */
    public GLProgram(GLApi gl, String vertexSource, String fragmentSource) {
        this.gl = gl;
        int vertex = compile(GL_VERTEX_SHADER, gl.shaderPreamble() + vertexSource);
        int fragment;
        try {
            fragment = compile(GL_FRAGMENT_SHADER, gl.shaderPreamble() + fragmentSource);
        } catch (RuntimeException e) {
            gl.deleteShader(vertex);
            throw e;
        }
        program = gl.createProgram();
        gl.attachShader(program, vertex);
        gl.attachShader(program, fragment);
        gl.linkProgram(program);
        gl.deleteShader(vertex); // flagged; freed with the program
        gl.deleteShader(fragment);
        if (gl.getProgrami(program, GL_LINK_STATUS) != GL_TRUE) {
            String log = gl.getProgramInfoLog(program);
            gl.deleteProgram(program);
            throw new IllegalStateException("Shader program failed to link:\n" + log);
        }
    }

    private int compile(int type, String source) {
        int shader = gl.createShader(type);
        gl.shaderSource(shader, source);
        gl.compileShader(shader);
        if (gl.getShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE) {
            String log = gl.getShaderInfoLog(shader);
            gl.deleteShader(shader);
            throw new IllegalStateException((type == GL_VERTEX_SHADER ? "Vertex" : "Fragment")
                    + " shader failed to compile:\n" + log);
        }
        return shader;
    }

    public int id() {
        return program;
    }

    public void bind() {
        gl.useProgram(program);
    }

    public void unbind() {
        gl.useProgram(0);
    }

    /**
     * @return The uniform's location, or -1 if the program has no active uniform with that name.
     */
    public int uniformLocation(String name) {
        return gl.getUniformLocation(program, name);
    }

    /**
     * Bind a uniform block of this program to a uniform buffer binding point.
     *
     * @throws IllegalStateException If the program has no such block.
     */
    public void bindUniformBlock(String name, int binding) {
        int index = gl.getUniformBlockIndex(program, name);
        if (index == GL_INVALID_INDEX) {
            throw new IllegalStateException("Uniform block not found: " + name);
        }
        gl.uniformBlockBinding(program, index, binding);
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            gl.deleteProgram(program);
        }
    }

    /**
     * Read a shader source packaged with the engine (a class path resource; works on desktop and Android).
     *
     * @param path Resource path, e.g. {@code engine/shader/opengl/urp/sprite.vert.glsl}.
     * @throws IllegalStateException If the resource does not exist.
     */
    public static String loadSource(String path) {
        ClassLoader loader = GLProgram.class.getClassLoader();
        try (InputStream in = loader.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Shader source not found on the class path: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read shader source " + path, e);
        }
    }
}
