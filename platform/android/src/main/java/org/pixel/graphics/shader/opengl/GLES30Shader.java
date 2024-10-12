package org.pixel.graphics.shader.opengl;

import android.opengl.GLES30;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.graphics.shader.Shader;

import java.util.HashMap;
import java.util.List;

public abstract class GLES30Shader implements Shader {

    private static final Logger log = LoggerFactory.getLogger(GLES30Shader.class);

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
    public GLES30Shader(String vertexSrc, String fragSrc, List<String> attributes, List<String> uniforms) {
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
        vertexShaderId = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER);
        fragmentShaderId = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER);

        // attach shader source
        initShader(vertexShaderId, vertexShaderSource);
        initShader(fragmentShaderId, fragmentShaderSource);

        // create program:
        programId = GLES30.glCreateProgram();

        // attach shaders:
        GLES30.glAttachShader(programId, vertexShaderId);
        GLES30.glAttachShader(programId, fragmentShaderId);

        // link:
        GLES30.glLinkProgram(programId);

        // status check:
        int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES30.GL_TRUE) {
            throw new RuntimeException("Could not link program: " + GLES30.glGetProgramInfoLog(programId));
        }

        // cache locations
        cacheAttributeLocations();
        cacheUniformLocations();

        return true;
    }

    @Override
    public void use() {
        GLES30.glUseProgram(programId);
    }

    @Override
    public void apply() {
        // intentionally empty
    }

    @Override
    public void dispose() {
        GLES30.glDeleteShader(vertexShaderId);
        GLES30.glDeleteShader(fragmentShaderId);
        GLES30.glDeleteProgram(programId);
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
        GLES30.glShaderSource(shaderId, shaderSrc);
        GLES30.glCompileShader(shaderId);

        int[] linkStatus = new int[1];
        GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES30.GL_TRUE) {
            throw new RuntimeException(GLES30.glGetShaderInfoLog(shaderId));
        }
    }

    private void cacheAttributeLocations() {
        attributes.forEach(elem -> attributeLocationMap.put(elem, GLES30.glGetAttribLocation(programId, elem)));
    }

    private void cacheUniformLocations() {
        uniforms.forEach(elem -> uniformLocationMap.put(elem, GLES30.glGetUniformLocation(programId, elem)));
    }
}
