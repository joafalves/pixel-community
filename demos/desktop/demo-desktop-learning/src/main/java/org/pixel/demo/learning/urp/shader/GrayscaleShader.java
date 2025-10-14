package org.pixel.demo.learning.urp.shader;

import org.pixel.graphics.shader.opengl.GLShader;

import java.util.Arrays;
import java.util.List;

/**
 * A simple shader that applies a grayscale effect.
 */
public class GrayscaleShader extends GLShader {

    private static final List<String> uniforms = Arrays.asList("uMatrix", "uTextureImage", "u_intensity");
    // Attributes must match the new, simpler vertex shader
    private static final List<String> attributes = Arrays.asList("aVertexPosition", "aTextureCoord");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        // Use the new, simple vertex shader created for direct rendering
        vertSrc = loadShader("engine/shader/opengl/renderable.vert.glsl");
        //fragSrc = loadShader("engine/shader/opengl/renderable.frag.glsl");
        fragSrc = loadShader("shaders/grayscale.frag.glsl");
    }

    /**
     * Constructor.
     */
    public GrayscaleShader() {
        super(vertSrc, fragSrc, attributes, uniforms);
        init();
    }
}
