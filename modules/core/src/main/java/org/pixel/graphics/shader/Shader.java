/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader;

import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.opengl.GL20C.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20C.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20C.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20C.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20C.glAttachShader;
import static org.lwjgl.opengl.GL20C.glCompileShader;
import static org.lwjgl.opengl.GL20C.glCreateProgram;
import static org.lwjgl.opengl.GL20C.glCreateShader;
import static org.lwjgl.opengl.GL20C.glDeleteProgram;
import static org.lwjgl.opengl.GL20C.glDeleteShader;
import static org.lwjgl.opengl.GL20C.glGetAttribLocation;
import static org.lwjgl.opengl.GL20C.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20C.glGetProgrami;
import static org.lwjgl.opengl.GL20C.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20C.glGetShaderi;
import static org.lwjgl.opengl.GL20C.glGetUniformLocation;
import static org.lwjgl.opengl.GL20C.glLinkProgram;
import static org.lwjgl.opengl.GL20C.glShaderSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;

public abstract class Shader implements Disposable {

    //region private properties

    private static final Logger log = LoggerFactory.getLogger(Shader.class);

    private final String vertSrc;
    private final String fragSrc;

    private int vertId;
    private int fragId;
    private int programId;

    private final List<String> attributes;
    private final List<String> uniforms;
    private final HashMap<String, Integer> attributeLocationMap;
    private final HashMap<String, Integer> uniformLocationMap;

    //endregion

    //region constructors

    /**
     * Constructor.
     *
     * @param vertexSrc  The vertex shader source.
     * @param fragSrc    The fragment shader source.
     * @param attributes The shader attributes.
     * @param uniforms   The shader uniforms.
     */
    public Shader(String vertexSrc, String fragSrc, List<String> attributes, List<String> uniforms) {
        this.vertSrc = vertexSrc;
        this.fragSrc = fragSrc;
        this.attributes = attributes;
        this.uniforms = uniforms;
        this.attributeLocationMap = new HashMap<>();
        this.uniformLocationMap = new HashMap<>();

        this.setup();
    }

    //endregion

    //region private methods

    private void setup() {
        // create and put vertex/fragment shader:
        vertId = glCreateShader(GL_VERTEX_SHADER);
        fragId = glCreateShader(GL_FRAGMENT_SHADER);

        // attach shader source
        setupShader(vertId, vertSrc);
        setupShader(fragId, fragSrc);

        // create program:
        programId = glCreateProgram();

        // attach shaders:
        glAttachShader(programId, vertId);
        glAttachShader(programId, fragId);

        // link:
        glLinkProgram(programId);

        // status check:
        if (glGetProgrami(programId, GL_LINK_STATUS) != GL_TRUE) {
            throw new RuntimeException(glGetProgramInfoLog(programId));
        }

        // cache locations
        cacheAttributeLocations();
        cacheUniformLocations();
    }

    private void setupShader(int shaderId, CharSequence shaderSrc) {
        // attach source & compile shader:
        glShaderSource(shaderId, shaderSrc);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) != GL_TRUE) {
            throw new RuntimeException(glGetShaderInfoLog(shaderId));
        }
    }

    private void cacheAttributeLocations() {
        attributes.forEach(elem -> attributeLocationMap.put(elem, glGetAttribLocation(programId, elem)));
    }

    private void cacheUniformLocations() {
        uniforms.forEach(elem -> uniformLocationMap.put(elem, glGetUniformLocation(programId, elem)));
    }

    //endregion

    //region public methods

    /**
     * Apply shader values.
     */
    public abstract void apply();

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

    @Override
    public void dispose() {
        glDeleteShader(vertId);
        glDeleteShader(fragId);
        glDeleteProgram(programId);
    }

    //endregion

    //region getters & setters

    /**
     * Get the native program id.
     *
     * @return The native program id.
     */
    public int getProgramId() {
        return this.programId;
    }

    //endregion

    // region static

    /**
     * Load shader source from file.
     *
     * @param fileName The file name.
     * @return The shader source.
     */
    protected static String loadShader(String fileName) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) {
                return null;
            }
            try (InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (IOException e) {
            log.error("Exception caught!", e);
        }

        return "";
    }

    // endregion

}
