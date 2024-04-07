package org.pixel.graphics.shader.opengl;

import java.util.Arrays;
import java.util.List;

public class GLESTextureShader extends GLESShader {

    //region private properties

    private static final List<String> uniforms = Arrays.asList("uMatrix", "uTextureImage");
    private static final List<String> attributes = Arrays.asList("aVertexPosition", "aVertexColor", "aTextureCoordinates");

    //private static final String vertSrc;
    //private static final String fragSrc;

    static {
        //vertSrc = loadShader("engine/shader/opengl/texture.vert.glsl");
        //fragSrc = loadShader("engine/shader/opengl/texture.frag.glsl");
    }

    //endregion

    //region constructors

    /**
     * Constructor.
     */
    public GLESTextureShader() {
        super(null, null, attributes, uniforms);
        this.init();
    }

    //endregion
}
