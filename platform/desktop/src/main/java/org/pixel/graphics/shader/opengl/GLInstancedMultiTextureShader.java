package org.pixel.graphics.shader.opengl;

import java.util.Arrays;
import java.util.List;

/**
 * A shader that supports instanced rendering with multiple textures.
 */
public class GLInstancedMultiTextureShader extends GLShader {

    private static final List<String> uniforms = Arrays.asList("uMatrix", "uTextureImage");
    private static final List<String> attributes = Arrays.asList(
            "aVertexPosition", "iPosition", "iSize", "iAnchor", "iRotation",
            "iColor", "iSource", "iTexIndex"
    );

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/opengl/instanced_multitex.vert.glsl");
        fragSrc = loadShader("engine/shader/opengl/instanced_multitex.frag.glsl");
    }

    /**
     * Constructor.
     *
     * @param textureCount The number of textures to be used.
     */
    public GLInstancedMultiTextureShader(int textureCount) {
        super(vertSrc,
                fragSrc
                        .replace("/*$numTextures*/", String.valueOf(textureCount))
                        .replace("/*$textureSwitchCase*/", createTextureSwitch(textureCount)),
                attributes, uniforms);
        this.init();
    }

    /**
     * Creates the GLSL switch statement for texture selection.
     *
     * @param textureCount The number of textures to support.
     * @return The generated switch statement as a string.
     */
    private static String createTextureSwitch(int textureCount) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < textureCount; i++) {
            sb.append("\tcase ").append(i).append(":\n");
            sb.append("\t\tcolor = texture(uTextureImage[").append(i).append("], vTextureCoord); ");
            sb.append("\t\tbreak; ");
        }
        sb.append("\tdefault:\n");
        sb.append("\t\tcolor = vec4(1.0, 0.0, 1.0, 1.0); "); // Error color

        return sb.toString();
    }
}
