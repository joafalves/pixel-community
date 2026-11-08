package org.pixel.graphics.opengl.render;

import org.pixel.graphics.backend.BatchList;
import org.pixel.graphics.opengl.GLApi;
import org.pixel.graphics.opengl.GLProgram;

/**
 * The URP v2 shape program ({@code VertexLayout.FULL}): SDF shapes with fill and stroke, distance-field glyphs,
 * textured quads, per-vertex clipping. Uses two uniform blocks bound at {@link #CLIP_BINDING} and
 * {@link #STYLE_BINDING}.
 */
final class GLShapeProgram extends GLProgram {

    static final int CLIP_BINDING = 0;
    static final int STYLE_BINDING = 1;

    private static final String VERTEX_PATH = "engine/shader/opengl/urp/shape.vert.glsl";
    private static final String FRAGMENT_PATH = "engine/shader/opengl/urp/shape.frag.glsl";

    GLShapeProgram(GLApi gl, int textureSlots, int maxClips, int maxStyles) {
        super(gl, loadSource(VERTEX_PATH),
                loadSource(FRAGMENT_PATH)
                        .replace("/*$numTextures*/", String.valueOf(textureSlots))
                        .replace("/*$maxClips*/", String.valueOf(maxClips))
                        .replace("/*$maxStyleVec4*/", String.valueOf(maxStyles * BatchList.STYLE_FLOATS / 4))
                        .replace("/*$sampleSwitchCase*/", sampleSwitch(textureSlots)));
        bindUniformBlock("ClipBlock", CLIP_BINDING);
        bindUniformBlock("StyleBlock", STYLE_BINDING);
    }

    /** {@code case i: texel = texture(uTextures[i], uv);} per slot (sampler arrays need constant indices). */
    private static String sampleSwitch(int slots) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < slots; i++) {
            sb.append("        case ").append(i).append(": texel = texture(uTextures[").append(i)
                    .append("], uv); break;\n");
        }
        return sb.toString();
    }
}
