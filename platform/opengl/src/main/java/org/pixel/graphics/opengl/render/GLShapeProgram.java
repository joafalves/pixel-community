package org.pixel.graphics.opengl.render;

import org.pixel.graphics.backend.BatchList;
import org.pixel.graphics.opengl.GLApi;
import org.pixel.graphics.opengl.GLProgram;

/**
 * The URP v2 shape program ({@code VertexLayout.FULL}): SDF shapes with fill and stroke, distance-field glyphs,
 * textured quads, per-vertex clipping. Uses two uniform blocks bound at {@link #CLIP_BINDING} and
 * {@link #STYLE_BINDING}. Also builds material programs for the same vertex layout: effect materials (this program
 * with an {@code effect()} function inserted) and raw materials (the user's shaders).
 */
final class GLShapeProgram extends GLProgram {

    static final int CLIP_BINDING = 0;
    static final int STYLE_BINDING = 1;

    private static final String VERTEX_PATH = "engine/shader/opengl/urp/shape.vert.glsl";
    private static final String FRAGMENT_PATH = "engine/shader/opengl/urp/shape.frag.glsl";

    private GLShapeProgram(GLApi gl, String vertexSource, String fragmentSource, boolean requireBlocks) {
        super(gl, vertexSource, fragmentSource);
        try {
            bindBlock("ClipBlock", CLIP_BINDING, requireBlocks);
            bindBlock("StyleBlock", STYLE_BINDING, requireBlocks);
        } catch (RuntimeException e) {
            dispose();
            throw e;
        }
    }

    /**
     * The built-in shape program.
     */
    GLShapeProgram(GLApi gl, int textureSlots, int maxClips, int maxStyles) {
        this(gl, loadSource(VERTEX_PATH), fragment(textureSlots, maxClips, maxStyles, null), true);
    }

    /**
     * The shape program with an effect material's {@code effect()} function.
     */
    static GLShapeProgram effect(GLApi gl, int textureSlots, int maxClips, int maxStyles, String effectSource) {
        return new GLShapeProgram(gl, loadSource(VERTEX_PATH),
                fragment(textureSlots, maxClips, maxStyles, effectSource), true);
    }

    /**
     * A raw material's shaders. The fragment shader is prefixed with {@code PIXEL_TEXTURE_SLOTS},
     * {@code uTextures}, {@code uPremultiplied} and {@code pixelSample(slot, uv)} (premultiplied); the uniform blocks
     * are bound if the shaders declare them.
     */
    static GLShapeProgram raw(GLApi gl, int textureSlots, String vertexSource, String fragmentSource) {
        String header = "#define PIXEL_TEXTURE_SLOTS " + textureSlots + "\n"
                + "uniform sampler2D uTextures[PIXEL_TEXTURE_SLOTS];\n"
                + "uniform int uPremultiplied;\n"
                + "vec4 pixelSample(int slot, vec2 uv) {\n"
                + "    vec4 texel = vec4(1.0);\n"
                + "    switch (slot) {\n" + GLSpriteProgram.textureSwitch(textureSlots) + "    }\n"
                + "    if (((uPremultiplied >> slot) & 1) == 0) {\n"
                + "        texel.rgb *= texel.a;\n"
                + "    }\n"
                + "    return texel;\n"
                + "}\n"
                + "#line 1\n";
        return new GLShapeProgram(gl, vertexSource, header + fragmentSource, false);
    }

    private static String fragment(int textureSlots, int maxClips, int maxStyles, String effectSource) {
        return loadSource(FRAGMENT_PATH)
                .replace("/*$defines*/", effectSource != null ? "#define PIXEL_MATERIAL" : "")
                .replace("/*$numTextures*/", String.valueOf(textureSlots))
                .replace("/*$maxClips*/", String.valueOf(maxClips))
                .replace("/*$maxStyleVec4*/", String.valueOf(maxStyles * BatchList.STYLE_FLOATS / 4))
                .replace("/*$sampleSwitchCase*/", GLSpriteProgram.textureSwitch(textureSlots))
                .replace("/*$material*/", effectSource != null ? "#line 1\n" + effectSource : "");
    }

    private void bindBlock(String name, int binding, boolean required) {
        if (required || hasUniformBlock(name)) {
            bindUniformBlock(name, binding);
        }
    }
}
