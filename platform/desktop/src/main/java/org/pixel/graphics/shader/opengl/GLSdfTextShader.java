/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.opengl;

import java.util.Arrays;
import java.util.List;

/**
 * Shader for rendering SDF (Signed Distance Field) text with stroke/outline support.
 */
public class GLSdfTextShader extends GLShader {

    private static final List<String> attributes = Arrays.asList("aPosition", "aTexCoord");
    private static final List<String> uniforms = Arrays.asList(
        "uViewMatrix", "uAtlas", "uFillColor", "uStrokeColor", "uStrokeWidth", "uSmoothness"
    );

    private static final String vertSrc = loadShader("engine/shader/opengl/sdf_text.vert.glsl");
    private static final String fragSrc = loadShader("engine/shader/opengl/sdf_text.frag.glsl");

    /**
     * Constructor.
     */
    public GLSdfTextShader() {
        super(vertSrc, fragSrc, attributes, uniforms);
        init();
    }
}
