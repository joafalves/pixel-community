package org.pixel.demo.learning.urp.shader;

import org.pixel.graphics.shader.opengl.GLShader;

import java.util.Arrays;
import java.util.List;

/**
 * A shader that applies a glowing effect to the sprite.
 */
public class GlowShader extends GLShader {

    private static final List<String> uniforms = Arrays.asList("uMatrix", "uTextureImage", "u_glowIntensity");
    private static final List<String> attributes = Arrays.asList("aVertexPosition", "aTextureCoord");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/opengl/renderable.vert.glsl");
        fragSrc = loadShader("shaders/glow.frag.glsl");
    }

    /**
     * Constructor.
     */
    public GlowShader() {
        super(vertSrc, fragSrc, attributes, uniforms);
        init();
    }
}
