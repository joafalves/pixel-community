package org.pixel.graphics.opengl.render;

import org.pixel.graphics.backend.BatchList;
import org.pixel.graphics.shader.opengl.GLShader;

import java.util.List;

import static org.lwjgl.opengl.GL31C.GL_INVALID_INDEX;
import static org.lwjgl.opengl.GL31C.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31C.glUniformBlockBinding;

/**
 * The URP v2 shape program ({@code VertexLayout.FULL}): SDF shapes with fill and stroke, textured quads, per-vertex
 * clipping. Uses two uniform blocks bound at {@link #CLIP_BINDING} and {@link #STYLE_BINDING}.
 */
final class GLShapeProgram extends GLShader {

    static final int CLIP_BINDING = 0;
    static final int STYLE_BINDING = 1;

    private static final String VERTEX_PATH = "engine/shader/opengl/urp/shape.vert.glsl";
    private static final String FRAGMENT_PATH = "engine/shader/opengl/urp/shape.frag.glsl";

    GLShapeProgram(int textureSlots, int maxClips, int maxStyles) {
        super(GLSpriteProgram.DESKTOP_PREAMBLE + GLSpriteProgram.require(VERTEX_PATH),
                GLSpriteProgram.DESKTOP_PREAMBLE + GLSpriteProgram.require(FRAGMENT_PATH)
                        .replace("/*$numTextures*/", String.valueOf(textureSlots))
                        .replace("/*$maxClips*/", String.valueOf(maxClips))
                        .replace("/*$maxStyleVec4*/", String.valueOf(maxStyles * BatchList.STYLE_FLOATS / 4))
                        .replace("/*$textureSwitchCase*/", GLSpriteProgram.textureSwitch(textureSlots)),
                List.of(), List.of(GLSpriteProgram.U_MATRIX, GLSpriteProgram.U_TEXTURES));
        init();
        bindBlock("ClipBlock", CLIP_BINDING);
        bindBlock("StyleBlock", STYLE_BINDING);
    }

    private void bindBlock(String name, int binding) {
        int index = glGetUniformBlockIndex(getProgramId(), name);
        if (index == GL_INVALID_INDEX) {
            throw new IllegalStateException("Uniform block not found in shape program: " + name);
        }
        glUniformBlockBinding(getProgramId(), index, binding);
    }
}
