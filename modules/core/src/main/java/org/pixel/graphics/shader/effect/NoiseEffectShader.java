/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.effect;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform2f;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.pixel.graphics.shader.Shader;
import org.pixel.math.Vector2;

@Getter
@Setter
public class NoiseEffectShader extends Shader {

    private static final List<String> uniforms = Arrays.asList("uTextureImage", "uAmount", "uOffset", "uDensity");
    private static final List<String> attributes = Collections.singletonList("aVertexPosition");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/effect/noise.vert.glsl");
        fragSrc = loadShader("engine/shader/effect/noise.frag.glsl");
    }

    private float amount;
    private float density;
    private Vector2 offset;

    /**
     * Constructor.
     *
     * @param amount  The amount of noise to apply (0-1).
     * @param density The density of the noise (0-1).
     */
    public NoiseEffectShader(float amount, float density) {
        super(vertSrc, fragSrc, attributes, uniforms);
        this.amount = amount;
        this.density = density;
        this.offset = Vector2.zero();
    }

    @Override
    public void apply() {
        glUniform1f(getUniformLocation("uAmount"), amount);
        glUniform1f(getUniformLocation("uDensity"), density);
        glUniform2f(getUniformLocation("uOffset"), offset.getX(), offset.getY());
    }
}

