package org.pixel.graphics.opengl.render;

import org.pixel.graphics.opengl.GLApi;
import org.pixel.graphics.opengl.GLProgram;

import java.nio.FloatBuffer;

/**
 * A batch program plus the uniform state the backend tracks for it: the view matrix (uploaded once per pass that
 * uses the program) and the premultiplied-slot mask.
 */
class ProgramState {

    static final String U_MATRIX = "uMatrix";
    static final String U_TEXTURES = "uTextures";
    static final String U_PREMULTIPLIED = "uPremultiplied";

    final GLApi gl;
    final GLProgram program;
    private final int matrixLocation;
    private final int premultipliedLocation;
    private int matrixPass = -1;      // pass serial of the last matrix upload
    private int premultipliedMask = -1; // last value sent (-1 = unknown)

    /**
     * Point {@code uTextures[i]} at texture unit i. Leaves no program bound.
     */
    ProgramState(GLApi gl, GLProgram program, int textureSlots) {
        this.gl = gl;
        this.program = program;
        this.matrixLocation = program.uniformLocation(U_MATRIX);
        this.premultipliedLocation = program.uniformLocation(U_PREMULTIPLIED);
        int[] units = new int[textureSlots];
        for (int i = 0; i < textureSlots; i++) {
            units[i] = i;
        }
        program.bind();
        int texturesLocation = program.uniformLocation(U_TEXTURES);
        if (texturesLocation >= 0) {
            gl.uniform1iv(texturesLocation, units);
        }
        program.unbind();
    }

    /** Upload the pass's matrix if this program hasn't seen this pass yet (must be bound). */
    void matrix(FloatBuffer matrix, int pass) {
        if (matrixPass != pass) {
            if (matrixLocation >= 0) {
                gl.uniformMatrix4fv(matrixLocation, matrix);
            }
            matrixPass = pass;
            premultipliedMask = -1; // a new pass: don't trust what other renderers may have done
        }
    }

    /** Which texture slots hold premultiplied colors (bit per slot; must be bound). */
    void premultiplied(int mask) {
        if (mask != premultipliedMask && premultipliedLocation >= 0) {
            gl.uniform1i(premultipliedLocation, mask);
            premultipliedMask = mask;
        }
    }

    void dispose() {
        program.dispose();
    }
}
