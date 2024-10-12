/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.opengl;

import java.util.Collections;
import java.util.List;

public class GLRenderBufferShader extends GLShader {

    private static final List<String> uniforms = Collections.singletonList("uTextureImage");
    private static final List<String> attributes = Collections.singletonList("aVertexPosition");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/opengl/renderBuffer.vert.glsl");
        fragSrc = loadShader("engine/shader/opengl/renderBuffer.frag.glsl");
    }

    /**
     * Constructor.
     */
    public GLRenderBufferShader() {
        super(vertSrc, fragSrc, attributes, uniforms);
        this.init();
    }

}
