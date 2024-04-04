/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.opengl.effect;

import lombok.Getter;
import lombok.Setter;
import org.pixel.graphics.shader.opengl.GLShader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL20C.glUniform1f;

@Getter
@Setter
public class GLSepiaEffectShader extends GLShader {

    private static final List<String> uniforms = Arrays.asList("uTextureImage", "uAmount", "uTime");
    private static final List<String> attributes = Collections.singletonList("aVertexPosition");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/opengl/effect/sepia.vert.glsl");
        fragSrc = loadShader("engine/shader/opengl/effect/sepia.frag.glsl");
    }

    private float amount;

    /**
     * Constructor.
     *
     * @param amount The amount of sepia (0 to 1).
     */
    public GLSepiaEffectShader(float amount) {
        super(vertSrc, fragSrc, attributes, uniforms);
        this.amount = amount;
        this.init();
    }

    @Override
    public void apply() {
        glUniform1f(getUniformLocation("uAmount"), amount);
    }
}
