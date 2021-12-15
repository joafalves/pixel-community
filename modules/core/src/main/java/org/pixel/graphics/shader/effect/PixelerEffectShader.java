/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.effect;

import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20C.glUniform1i;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.pixel.graphics.shader.Shader;

@Getter
@Setter
public class PixelerEffectShader extends Shader {

    private static final List<String> uniforms = Arrays.asList("uTextureImage", "uBlockSizeH", "uBlockSizeV",
            "uAnimated", "uTime");
    private static final List<String> attributes = Arrays.asList("aVertexPosition");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/effect/pixeler.vert.glsl");
        fragSrc = loadShader("engine/shader/effect/pixeler.frag.glsl");
    }

    private boolean animated;
    private float horizontalBlockSize;
    private float verticalBlockSize;

    /**
     * Constructor.
     *
     * @param horizontalBlockSize The horizontal block size.
     * @param verticalBlockSize   The vertical block size.
     */
    public PixelerEffectShader(float horizontalBlockSize, float verticalBlockSize) {
        super(vertSrc, fragSrc, attributes, uniforms);
        this.horizontalBlockSize = horizontalBlockSize;
        this.verticalBlockSize = verticalBlockSize;
        this.animated = false;
    }

    @Override
    public void apply() {
        glUniform1f(getUniformLocation("uBlockSizeH"), horizontalBlockSize);
        glUniform1f(getUniformLocation("uBlockSizeV"), verticalBlockSize);
        glUniform1i(getUniformLocation("uAnimated"), animated ? 1 : 0);
    }
}
