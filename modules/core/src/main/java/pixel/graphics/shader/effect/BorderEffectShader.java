/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.graphics.shader.effect;

import pixel.graphics.Color;
import pixel.graphics.shader.Shader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform4f;

public class BorderEffectShader extends Shader {

    private static final List<String> uniforms = Arrays.asList("uTextureImage", "uVertical", "uHorizontal", "uTime", "uColor");
    private static final List<String> attributes = Collections.singletonList("aVertexPosition");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("shader/effect/border.vert.glsl");
        fragSrc = loadShader("shader/effect/border.frag.glsl");
    }

    private float vertical;
    private float horizontal;
    private Color color;

    /**
     * Constructor
     */
    public BorderEffectShader(float vertical, float horizontal) {
        super(vertSrc, fragSrc, attributes, uniforms);
        this.vertical = vertical;
        this.horizontal = horizontal;
        this.color = Color.BLACK;
    }

    /**
     * Constructor
     */
    public BorderEffectShader(float vertical, float horizontal, Color color) {
        super(vertSrc, fragSrc, attributes, uniforms);
        this.vertical = vertical;
        this.horizontal = horizontal;
        this.color = color;
    }

    /**
     * Apply shader values
     */
    @Override
    public void apply() {
        glUniform1f(getUniformLocation("uHorizontal"), horizontal);
        glUniform1f(getUniformLocation("uVertical"), vertical);
        glUniform4f(getUniformLocation("uColor"), color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }
}
