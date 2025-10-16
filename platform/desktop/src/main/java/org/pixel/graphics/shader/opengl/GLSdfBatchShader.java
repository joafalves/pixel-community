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
        "aShapeData",     // vec4: shape-specific data
        "aShapeType",     // float: shape type discriminator
        "aTextureId"      // float: texture atlas ID
    );
    
    private static final List<String> uniforms = Arrays.asList(
        "uViewMatrix",    // mat4: view-projection matrix
        "uTextAtlas",     // sampler2D: font texture atlas
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
        super(vertSrc, fragSrc, attributes, uniforms);
        this.init();
    }
}
