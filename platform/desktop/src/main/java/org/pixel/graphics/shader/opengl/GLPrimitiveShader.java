/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.opengl;

import java.util.Arrays;
import java.util.List;

public class GLPrimitiveShader extends GLShader {

    //region private properties

    private static final List<String> uniforms = List.of("uMatrix");
    private static final List<String> attributes = Arrays.asList("aVertexPosition", "aVertexColor");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/opengl/primitive.vert.glsl");
        fragSrc = loadShader("engine/shader/opengl/primitive.frag.glsl");
    }

    //endregion

    //region constructors

    /**
     * Constructor.
     */
    public GLPrimitiveShader() {
        super(vertSrc, fragSrc, attributes, uniforms);
        this.init();
    }

    //endregion
}
