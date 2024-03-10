/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.effect;

import lombok.Getter;
import lombok.Setter;

import org.pixel.commons.Color;
import org.pixel.graphics.shader.Shader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL20C.glUniform1f;
import static org.lwjgl.opengl.GL20C.glUniform4f;

@Getter
@Setter
public class BorderEffectShader extends Shader {

    private static final List<String> uniforms = Arrays.asList("uTextureImage", "uVertical", "uHorizontal", "uTime",
            "uColor");
    private static final List<String> attributes = Collections.singletonList("aVertexPosition");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/effect/border.vert.glsl");
        fragSrc = loadShader("engine/shader/effect/border.frag.glsl");
    }

    private float vertical;
    private float horizontal;
    private Color color;

    /**
     * Constructor.
     *
     * @param vertical   Vertical border size
     * @param horizontal Horizontal border size
     */
    public BorderEffectShader(float vertical, float horizontal) {
        super(vertSrc, fragSrc, attributes, uniforms);
        this.vertical = vertical;
        this.horizontal = horizontal;
        this.color = Color.BLACK;
        this.setup();
    }

    /**
     * Constructor.
     *
     * @param vertical   Vertical border size
     * @param horizontal Horizontal border size
     * @param color      Border color
     */
    public BorderEffectShader(float vertical, float horizontal, Color color) {
        super(vertSrc, fragSrc, attributes, uniforms);
        this.vertical = vertical;
        this.horizontal = horizontal;
        this.color = color;
    }

    @Override
    public void apply() {
        glUniform1f(getUniformLocation("uHorizontal"), horizontal);
        glUniform1f(getUniformLocation("uVertical"), vertical);
        glUniform4f(getUniformLocation("uColor"), color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }
}
