/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.opengl;

import java.util.Arrays;
import java.util.List;

/**
 * Unified SDF batch shader that handles all shape types and text rendering.
 * Uses a shape type discriminator to evaluate different SDF functions in the fragment shader.
 * 
 * Supported shape types:
 * - Rounded rectangles (filled and stroked)
 * - Circles (filled and stroked)
 * - Lines
 * - Points
 * - Text glyphs (SDF font atlas)
 */
public class GLSdfBatchShader extends GLShader {

    private static final List<String> attributes = Arrays.asList(
        "aPosition",      // vec2: vertex position
        "aTexCoord",      // vec2: texture coordinate
        "aColor",         // vec4: vertex color
        "aShapeData",     // vec4: shape-specific data part 1
        "aShapeDataExtra",// vec3: shape-specific data part 2
        "aQuadSize",      // vec2: quad size
        "aShapeType",     // float: shape type discriminator
        "aTextureId"      // float: texture atlas ID
    );
    
    private static final List<String> uniforms = Arrays.asList(
        "uViewMatrix",    // mat4: view-projection matrix
        "uTextAtlas",     // sampler2D: font texture atlas
        "uTextures",      // sampler2D[]: texture array for image rendering
        "uSmoothness",    // float: anti-aliasing smoothness
        "uTextEdge"       // float: SDF edge threshold for text rendering
    );

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/opengl/sdf_batch.vert.glsl");
        fragSrc = loadShader("engine/shader/opengl/sdf_batch.frag.glsl");
    }

    /**
     * Constructor.
     */
    public GLSdfBatchShader() {
        this(8); // Default to 8 textures
    }

    /**
     * Constructor with custom texture count.
     *
     * @param textureCount The number of textures to support
     */
    public GLSdfBatchShader(int textureCount) {
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
            sb.append("\t\tcase ").append(i).append(":\n");
            sb.append("\t\t\ttexColor = texture(uTextures[").append(i).append("], vTexCoord);\n");
            sb.append("\t\t\tbreak;\n");
        }
        sb.append("\t\tdefault:\n");
        sb.append("\t\t\ttexColor = vec4(1.0, 0.0, 1.0, 1.0); // Error: magenta\n");

        return sb.toString();
    }
}
