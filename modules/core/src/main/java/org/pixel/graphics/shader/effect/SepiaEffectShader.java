/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.effect;

import lombok.Getter;
import lombok.Setter;
import org.pixel.graphics.shader.Shader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL20C.glUniform1f;

@Getter
@Setter
public class SepiaEffectShader extends Shader {

    private static final List<String> uniforms = Arrays.asList("uTextureImage", "uAmount", "uTime");
    private static final List<String> attributes = Collections.singletonList("aVertexPosition");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/effect/sepia.vert.glsl");
        fragSrc = loadShader("engine/shader/effect/sepia.frag.glsl");
    }

    private float amount;

    /**
     * Constructor.
     *
     * @param amount The amount of sepia (0 to 1).
     */
    public SepiaEffectShader(float amount) {
        super(vertSrc, fragSrc, attributes, uniforms);
        this.amount = amount;
        this.setup();
    }

    @Override
    public void apply() {
        glUniform1f(getUniformLocation("uAmount"), amount);
    }
}
