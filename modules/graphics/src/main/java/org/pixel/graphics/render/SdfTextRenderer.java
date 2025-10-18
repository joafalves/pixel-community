package org.pixel.graphics.render;

import org.pixel.graphics.render.canvas.TextStyle;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.math.Matrix4;
import org.pixel.math.Vector2;

/**
 * Renderer interface for SDF (Signed Distance Field) based text.
 * Provides efficient text rendering using GPU fragment shaders with stroke support.
 */
public interface SdfTextRenderer extends Renderer {

    /**
     * Render text using SDF.
     *
     * @param text      The text to render
     * @param font      The SDF font to use
     * @param x         X position
     * @param y         Y position
     * @param style     Text style (color, stroke, etc.)
     * @param transform Transform matrix (combines local transform + view matrix)
     * @param scale     Scale factor (x, y) for the text
     */
    void render(String text, SdfFont font, float x, float y, TextStyle style, Matrix4 transform, Vector2 scale);
}
