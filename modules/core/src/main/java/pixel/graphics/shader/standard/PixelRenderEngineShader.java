/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.graphics.shader.standard;

import pixel.graphics.shader.Shader;

import java.util.Arrays;
import java.util.List;

public class PixelRenderEngineShader extends Shader {

    //region private properties

    private static final List<String> uniforms = Arrays.asList("uMatrix", "uTextureImage");
    private static final List<String> attributes = Arrays.asList("aVertexPosition", "aVertexColor", "aTextureCoordinates");

    private static final String vertSrc;
    private static final String fragSrc;

    static {
        vertSrc = loadShader("shader/standard/pixel.re2.vert.glsl");
        fragSrc = loadShader("shader/standard/pixel.re2.frag.glsl");
    }

    //endregion

    //region constructors

    /**
     * Constructor
     */
    public PixelRenderEngineShader() {
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
