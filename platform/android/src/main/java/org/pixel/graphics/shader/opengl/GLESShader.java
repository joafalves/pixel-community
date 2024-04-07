package org.pixel.graphics.shader.opengl;

import android.opengl.GLES20;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.graphics.shader.Shader;

import java.util.HashMap;
import java.util.List;

public abstract class GLESShader implements Shader {

    private static final Logger log = LoggerFactory.getLogger(GLESShader.class);

    private final String vertexShaderSource;
    private final String fragmentShaderSource;
    private final List<String> attributes;
    private final List<String> uniforms;
    private final HashMap<String, Integer> attributeLocationMap;
    private final HashMap<String, Integer> uniformLocationMap;

    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    /**
     * Constructor.
     *
     * @param vertexSrc  The vertex shader source.
     * @param fragSrc    The fragment shader source.
     * @param attributes The shader attributes.
     * @param uniforms   The shader uniforms.
     */
    public GLESShader(String vertexSrc, String fragSrc, List<String> attributes, List<String> uniforms) {
        this.vertexShaderSource = vertexSrc;
        this.fragmentShaderSource = fragSrc;
        this.attributes = attributes;
        this.uniforms = uniforms;
        this.attributeLocationMap = new HashMap<>();
        this.uniformLocationMap = new HashMap<>();
    }

    @Override
    public boolean init() {
        // create and put vertex/fragment shader:
        vertexShaderId = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        fragmentShaderId = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        // attach shader source
        initShader(vertexShaderId, vertexShaderSource);
        initShader(fragmentShaderId, fragmentShaderSource);

        // create program:
        programId = GLES20.glCreateProgram();

        // attach shaders:
        GLES20.glAttachShader(programId, vertexShaderId);
        GLES20.glAttachShader(programId, fragmentShaderId);

        // link:
        GLES20.glLinkProgram(programId);

        // status check:
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            throw new RuntimeException("Could not link program: " + GLES20.glGetProgramInfoLog(programId));
        }

        // cache locations
        cacheAttributeLocations();
        cacheUniformLocations();

        return true;
    }

    @Override
    public void use() {
        GLES20.glUseProgram(programId);
    }

    @Override
    public void apply() {
        // intentionally empty
    }

    @Override
    public void dispose() {
        GLES20.glDeleteShader(vertexShaderId);
        GLES20.glDeleteShader(fragmentShaderId);
        GLES20.glDeleteProgram(programId);
    }

    /**
     * Get the native program id.
     *
     * @return The native program id.
     */
    public int getProgramId() {
        return this.programId;
    }

    /**
     * Get shader uniform location.
     *
     * @param name The uniform name.
     * @return The uniform location.
     */
    public Integer getUniformLocation(String name) {
        return uniformLocationMap.get(name);
    }

    /**
     * Get shader attribute location.
     *
     * @param name The attribute name.
     * @return The attribute location.
     */
    public Integer getAttributeLocation(String name) {
        return attributeLocationMap.get(name);
    }

    private void initShader(int shaderId, String shaderSrc) {
        // attach source & compile shader:
        GLES20.glShaderSource(shaderId, shaderSrc);
        GLES20.glCompileShader(shaderId);

        int[] linkStatus = new int[1];
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            throw new RuntimeException(GLES20.glGetShaderInfoLog(shaderId));
        }
    }

    private void cacheAttributeLocations() {
        attributes.forEach(elem -> attributeLocationMap.put(elem, GLES20.glGetAttribLocation(programId, elem)));
    }

    private void cacheUniformLocations() {
        uniforms.forEach(elem -> uniformLocationMap.put(elem, GLES20.glGetUniformLocation(programId, elem)));
    }
}
