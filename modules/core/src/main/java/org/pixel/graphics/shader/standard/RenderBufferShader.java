/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.standard;

import org.pixel.graphics.shader.Shader;

import java.util.Collections;
import java.util.List;

public class RenderBufferShader extends Shader {

    private static final List<String> uniforms = Collections.singletonList("uTextureImage");
    private static final List<String> attributes = Collections.singletonList("aVertexPosition");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/standard/render.buffer.vert.glsl");
        fragSrc = loadShader("engine/shader/standard/render.buffer.frag.glsl");
    }

    /**
     * Constructor.
     */
    public RenderBufferShader() {
        super(vertSrc, fragSrc, attributes, uniforms);
    }

    @Override
    public void apply() {
        // nothing to apply
    }
}
