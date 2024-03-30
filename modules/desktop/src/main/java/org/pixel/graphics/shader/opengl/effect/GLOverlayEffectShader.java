/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.opengl.effect;

import lombok.Getter;
import lombok.Setter;

import org.pixel.commons.Color;
import org.pixel.graphics.shader.opengl.GLShader;

import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL20C.glUniform4f;

@Getter
@Setter
public class GLOverlayEffectShader extends GLShader {

    private static final List<String> uniforms = Arrays.asList("uTextureImage", "uOverlayColor", "uTime");
    private static final List<String> attributes = Arrays.asList("aVertexPosition");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/opengl/effect/overlay.vert.glsl");
        fragSrc = loadShader("engine/shader/opengl/effect/overlay.frag.glsl");
    }

    private Color overlayColor;

    /**
     * Constructor.
     *
     * @param overlayColor The overlay color.
     */
    public GLOverlayEffectShader(Color overlayColor) {
        super(vertSrc, fragSrc, attributes, uniforms);
        this.overlayColor = overlayColor;
        this.init();
    }

    @Override
    public void apply() {
        glUniform4f(getUniformLocation("uOverlayColor"), overlayColor.getRed(), overlayColor.getGreen(),
                overlayColor.getBlue(), overlayColor.getAlpha());
    }
}
