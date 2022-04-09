/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.standard;

import org.pixel.graphics.shader.Shader;

import java.util.Arrays;
import java.util.List;

public class MultiTextureShader extends Shader {

    //region private properties

    private static final List<String> uniforms = Arrays.asList("uMatrix", "uTextureImage");
    private static final List<String> attributes = Arrays.asList("aVertexPosition", "aVertexColor",
            "aTextureCoordinates", "aTextureIndex");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/standard/multiTexture.vert.glsl");
        fragSrc = loadShader("engine/shader/standard/multiTexture.frag.glsl");
    }

    //endregion

    //region constructors

    /**
     * Constructor.
     *
     * @param textureCount The number of textures to be used.
     */
    public MultiTextureShader(int textureCount) {
        super(vertSrc, fragSrc.replace("/*$numTextures*/", String.valueOf(textureCount)), attributes, uniforms);
        this.setup();
    }

    @Override
    public void apply() {

    }

    //endregion
}
