package org.pixel.graphics.opengl.render;

import org.pixel.graphics.shader.opengl.GLShader;

import java.util.List;

/**
 * The URP v2 sprite program (textured quads, {@code VertexLayout.SPRITE}), generated for a given number of texture
 * slots.
 */
final class GLSpriteProgram extends GLShader {

    /**
     * Prepended to every URP shader source. GLSL 3.30 core accepts precision qualifiers (no-op), so the same
     * sources compile for a future GLES 3.0 backend by only swapping the version line.
     */
    static final String DESKTOP_PREAMBLE = "#version 330 core\nprecision highp float;\nprecision highp int;\n";

    static final String U_MATRIX = "uMatrix";
    static final String U_TEXTURES = "uTextures";

    private static final String VERTEX_PATH = "engine/shader/opengl/urp/sprite.vert.glsl";
    private static final String FRAGMENT_PATH = "engine/shader/opengl/urp/sprite.frag.glsl";

    GLSpriteProgram(int textureSlots) {
        super(DESKTOP_PREAMBLE + require(VERTEX_PATH),
                DESKTOP_PREAMBLE + require(FRAGMENT_PATH)
                        .replace("/*$numTextures*/", String.valueOf(textureSlots))
                        .replace("/*$textureSwitchCase*/", textureSwitch(textureSlots)),
                List.of(), List.of(U_MATRIX, U_TEXTURES));
        init();
    }

    private static String require(String path) {
        String source = loadShader(path);
        if (source == null) {
            throw new IllegalStateException("Shader source not found on the classpath: " + path);
        }
        return source;
    }

    private static String textureSwitch(int slots) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < slots; i++) {
            sb.append("        case ").append(i).append(": color *= texture(uTextures[").append(i)
                    .append("], vTexCoord); break;\n");
        }
        return sb.toString();
    }
}
