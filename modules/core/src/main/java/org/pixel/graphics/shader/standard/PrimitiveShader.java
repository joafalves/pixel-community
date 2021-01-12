/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.shader.standard;

import org.pixel.graphics.shader.Shader;

import java.util.Arrays;
import java.util.List;

@Deprecated
public class PrimitiveShader extends Shader {

    //region private properties

    private static final List<String> uniforms = Arrays.asList("uMatrix", "uPointSize");
    private static final List<String> attributes = Arrays.asList("aVertexPosition", "aVertexColorPosition");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("shader/standard/primitive.vert.glsl");
        fragSrc = loadShader("shader/standard/primitive.frag.glsl");
    }

    //endregion

    //region constructors

    /**
     * Constructor
     */
    public PrimitiveShader() {
        super(vertSrc, fragSrc, attributes, uniforms);
    }

    /**
     * Apply shader values
     */
    @Override
    public void apply() {

    }

    //endregion
}
