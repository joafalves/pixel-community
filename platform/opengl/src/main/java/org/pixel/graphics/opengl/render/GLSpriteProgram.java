package org.pixel.graphics.opengl.render;

import org.pixel.graphics.opengl.GLApi;
import org.pixel.graphics.opengl.GLProgram;

/**
 * The URP v2 sprite program (textured quads, {@code VertexLayout.SPRITE}), generated for a given number of texture
 * slots.
 */
final class GLSpriteProgram extends GLProgram {

    static final String U_MATRIX = "uMatrix";
    static final String U_TEXTURES = "uTextures";

    private static final String VERTEX_PATH = "engine/shader/opengl/urp/sprite.vert.glsl";
    private static final String FRAGMENT_PATH = "engine/shader/opengl/urp/sprite.frag.glsl";

    GLSpriteProgram(GLApi gl, int textureSlots) {
        super(gl, loadSource(VERTEX_PATH),
                loadSource(FRAGMENT_PATH)
                        .replace("/*$numTextures*/", String.valueOf(textureSlots))
                        .replace("/*$textureSwitchCase*/", textureSwitch(textureSlots)));
    }

    /** {@code case i: color *= texture(uTextures[i], vTexCoord);} per slot (sampler arrays need constant indices). */
    private static String textureSwitch(int slots) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < slots; i++) {
            sb.append("        case ").append(i).append(": color *= texture(uTextures[").append(i)
                    .append("], vTexCoord); break;\n");
        }
        return sb.toString();
    }
}
