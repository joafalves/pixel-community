/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.opengl;

import org.pixel.io.FileUtils;

import java.util.Arrays;
import java.util.List;

public class GLES30TextureShader extends GLES30Shader {

    private static final List<String> uniforms = Arrays.asList("uMatrix", "uTextureImage");
    private static final List<String> attributes = Arrays.asList("aVertexPosition", "aVertexColor",
            "aTextureCoordinates");

    private static final String VERTEX_PATH = "engine/shader/gles30/texture.vert.glsl";
    private static final String FRAGMENT_PATH = "engine/shader/gles30/texture.frag.glsl";

    public GLES30TextureShader(android.content.Context context) {
        super(FileUtils.loadTextAsset(context, VERTEX_PATH), FileUtils.loadTextAsset(context, FRAGMENT_PATH),
                attributes, uniforms);
        this.init();
    }

}
