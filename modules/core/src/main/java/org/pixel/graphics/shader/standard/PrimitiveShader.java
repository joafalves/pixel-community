/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.standard;

import java.util.Arrays;
import java.util.List;
import org.pixel.graphics.shader.Shader;

public class PrimitiveShader extends Shader {

    //region private properties

    private static final List<String> uniforms = List.of("uMatrix");
    private static final List<String> attributes = Arrays.asList("aVertexPosition", "aVertexColor");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("engine/shader/standard/primitive.vert.glsl");
        fragSrc = loadShader("engine/shader/standard/primitive.frag.glsl");
    }

    //endregion

    //region constructors

    /**
     * Constructor.
     */
    public PrimitiveShader() {
        super(vertSrc, fragSrc, attributes, uniforms);
        this.setup();
    }

    @Override
    public void apply() {

    }

    //endregion
}
