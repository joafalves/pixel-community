package org.pixel.graphics.opengl;

import static org.lwjgl.opengl.GL11C.GL_LINES;
import static org.lwjgl.opengl.GL11C.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11C.GL_LINE_STRIP;
import static org.lwjgl.opengl.GL11C.GL_POINTS;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLE_STRIP;

public enum GLPrimitiveType {
    POINTS(GL_POINTS),
    LINES(GL_LINES),
    LINE_LOOP(GL_LINE_LOOP),
    LINE_STRIP(GL_LINE_STRIP),
    TRIANGLES(GL_TRIANGLES),
    TRIANGLE_STRIP(GL_TRIANGLE_STRIP),
    TRIANGLE_FAN(GL_TRIANGLE_FAN);

    private final int glMode;

    GLPrimitiveType(int glMode) {
        this.glMode = glMode;
    }

    /**
     * Get the OpenGL mode for this primitive type.
     *
     * @return OpenGL mode
     */
    public int getNativeMode() {
        return this.glMode;
    }
}
