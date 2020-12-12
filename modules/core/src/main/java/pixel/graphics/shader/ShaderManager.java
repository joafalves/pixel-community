/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package pixel.graphics.shader;

import static org.lwjgl.opengl.GL20.glUseProgram;

public class ShaderManager {

    //region private properties

    private static Shader activeShader = null;

    //endregion

    //region public static methods

    /**
     * Changes the active shader program. If the given shader program is already in use, no binding is applied.
     * Note that if glUseProgram() is called elsewhere it might cause conflicts and misbehaviour.
     *
     * @param shader
     */
    public static void useShader(Shader shader) {
        // swap needed?
        if (activeShader == null || activeShader.getProgramId() != shader.getProgramId()) {
            activeShader = shader;
            glUseProgram(shader.getProgramId());
        }
    }

    /**
     *
     */
    public static void clearActiveShader() {
        activeShader = null;
    }

    //endregion

}
