/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.opengl;

import org.pixel.io.FileUtils;

import java.util.Arrays;
import java.util.List;

public class GLES30TextureArrayShader extends GLES30Shader {

    private static final List<String> uniforms = Arrays.asList("uMatrix", "uTextureImage");
    private static final List<String> attributes = Arrays.asList("aVertexPosition", "aVertexColor",
            "aTextureCoordinates", "aTextureIndex");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = FileUtils.loadTextAsset("engine/shader/gles30/textureArray.vert.glsl");
        fragSrc = FileUtils.loadTextAsset("engine/shader/gles30/textureArray.frag.glsl");
    }

    /**
     * Constructor.
     */
    public GLES30TextureArrayShader() {
        super(vertSrc, fragSrc, attributes, uniforms);
        this.init();
    }

}
