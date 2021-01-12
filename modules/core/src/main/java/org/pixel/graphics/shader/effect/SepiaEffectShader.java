/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.effect;

import org.pixel.graphics.shader.Shader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL20.glUniform1f;

public class SepiaEffectShader extends Shader {

    private static final List<String> uniforms = Arrays.asList("uTextureImage", "uAmount", "uTime");
    private static final List<String> attributes = Collections.singletonList("aVertexPosition");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("shader/effect/sepia.vert.glsl");
        fragSrc = loadShader("shader/effect/sepia.frag.glsl");
    }

    private float amount;

    /**
     * Constructor
     */
    public SepiaEffectShader(float amount) {
        super(vertSrc, fragSrc, attributes, uniforms);
        this.amount = amount;
    }

    /**
     * Apply shader values
     */
    @Override
    public void apply() {
        glUniform1f(getUniformLocation("uAmount"), amount);
    }
}
