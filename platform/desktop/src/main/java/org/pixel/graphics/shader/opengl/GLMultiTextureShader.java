/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.opengl;

import java.util.Arrays;
import java.util.List;

public class GLMultiTextureShader extends GLShader {

    private static final List<String> uniforms = Arrays.asList("uMatrix", "uTextureImage");
    private static final List<String> attributes = Arrays.asList("aVertexPosition", "aVertexColor",
            "aTextureCoordinates", "aTextureIndex");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/opengl/multiTexture.vert.glsl");
        fragSrc = loadShader("engine/shader/opengl/multiTexture.frag.glsl");
    }

    /**
     * Constructor.
     *
     * @param textureCount The number of textures to be used.
     */
    public GLMultiTextureShader(int textureCount) {
        super(vertSrc,
                fragSrc
                        .replace("/*$numTextures*/", String.valueOf(textureCount))
                        .replace("/*$textureSwitchCase*/", createTextureSwitch(textureCount)),
                attributes, uniforms);
        this.init();
    }

    private static String createTextureSwitch(int textureCount) {
        var sb = new StringBuilder();
        for (int i = 0; i < textureCount; i++) {
            sb.append("\tcase ").append(i).append(":\n");
            sb.append("\t\tcolor = texture(uTextureImage[").append(i).append("], vTextureCoordinates) * vColor;\n");
            sb.append("\t\tbreak;\n");
        }
        return sb.toString();
    }
}
