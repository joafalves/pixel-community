package org.pixel.graphics.shader.opengl;

import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.graphics.shader.Shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11C.GL_TRUE;
import static org.lwjgl.opengl.GL20C.*;

public abstract class GLShader implements Shader {

    private static final Logger log = LoggerFactory.getLogger(GLShader.class);

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
    public GLShader(String vertexSrc, String fragSrc, List<String> attributes, List<String> uniforms) {
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
        vertexShaderId = glCreateShader(GL_VERTEX_SHADER);
        fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER);

        // attach shader source
        initShader(vertexShaderId, vertexShaderSource);
        initShader(fragmentShaderId, fragmentShaderSource);

        // create program:
        programId = glCreateProgram();

        // attach shaders:
        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);

        // link:
        glLinkProgram(programId);

        // status check:
        if (glGetProgrami(programId, GL_LINK_STATUS) != GL_TRUE) {
            throw new RuntimeException(glGetProgramInfoLog(programId));
        }

        // cache locations
        cacheAttributeLocations();
        cacheUniformLocations();

        return true;
    }

    @Override
    public void use() {
        glUseProgram(programId);
    }

    @Override
    public void apply() {
       // intentionally empty
    }

    @Override
    public void dispose() {
        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);
        glDeleteProgram(programId);
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

    private void initShader(int shaderId, CharSequence shaderSrc) {
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

    // region static

    /**
     * Load shader source from file.
     *
     * @param fileName The file name.
     * @return The shader source.
     */
    protected static String loadShader(String fileName) {
        // TODO: remove this code, use FileUtils.*
        // TODO: apply early loading? (load all engine shaders at once)
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
