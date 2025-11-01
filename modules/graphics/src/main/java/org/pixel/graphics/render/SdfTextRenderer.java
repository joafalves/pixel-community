package org.pixel.graphics.render;

import org.pixel.commons.factory.FactoryProvider;
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
     * Create a platform-specific SdfTextRenderer instance.
     *
     * <p>This factory method delegates to the registered {@link SdfTextRendererFactory} to create
     * the appropriate platform-specific implementation (e.g., GLSdfTextRenderer on desktop).
     *
     * <p>Example usage:
     * <pre>
     * SdfTextRenderer textRenderer = SdfTextRenderer.create();
     * textRenderer.render(text, font, x, y, style, transform, scale);
     * </pre>
     *
     * @return A new SdfTextRenderer instance appropriate for the current platform
     */
    static SdfTextRenderer create() {
        return FactoryProvider.get(SdfTextRendererFactory.class).create();
    }

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
