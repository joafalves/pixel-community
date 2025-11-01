/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.opengl;

import java.util.Arrays;
import java.util.List;

/**
 * Shader for rendering SDF-based shapes (rounded rectangles, circles, etc.).
 * Uses signed distance fields for perfect edges at any scale.
 */
public class GLSdfShapeShader extends GLShader {

    private static final List<String> uniforms = List.of("uMatrix", "uColor", "uSize", "uQuadSize", "uRadius", "uSmoothness", "uStrokeWidth", "uShapeType");
    private static final List<String> attributes = Arrays.asList("aPosition", "aTexCoord");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/opengl/sdf_shape.vert.glsl");
        fragSrc = loadShader("engine/shader/opengl/sdf_shape.frag.glsl");
    }

    /**
     * Constructor.
     */
    public GLSdfShapeShader() {
        super(vertSrc, fragSrc, attributes, uniforms);
        this.init();
    }
}
