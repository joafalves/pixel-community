package org.pixel.graphics.render.renderable;

import org.pixel.content.Font;
import org.pixel.graphics.render.Renderable;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

/**
 * A renderable for drawing text.
 * Uses SpriteBatch for efficient text rendering.
 */
public class TextRenderable extends Renderable<SpriteBatch> {
    private Font font;
    private String text;
    private int fontSize;
    private Rectangle cachedBounds; // Cached bounding box for culling

    /**
     * Public constructor.
     */
    public TextRenderable() {
        super(SpriteBatch.class);
        this.text = "";
        this.fontSize = 12;
    }

    @Override
    public void render(SpriteBatch spriteBatch, Matrix4 viewMatrix) {
        if (font != null && text != null) {
            spriteBatch.drawText(font, text, position, color, fontSize);
        }
    }

    @Override
    public Rectangle getBounds() {
        if (font == null || text == null || text.isEmpty()) {
            return null; // No font or empty text = no bounds
        }

        // Estimate text bounds based on font metrics
        // This is an approximation - actual rendering may vary slightly
        float scaleFactor = (float) fontSize / (float) font.getFontSize();
        float estimatedWidth = text.length() * fontSize * 0.6f; // Rough estimate
        float estimatedHeight = fontSize * scaleFactor;

        if (cachedBounds == null) {
            cachedBounds = new Rectangle(position.getX(), position.getY(), estimatedWidth, estimatedHeight);
        } else {
            cachedBounds.set(position.getX(), position.getY(), estimatedWidth, estimatedHeight);
        }
        return cachedBounds;
    }

    //<editor-fold desc="Text-Specific Getters and Setters">
    public Font getFont() { return font; }
    public TextRenderable setFont(Font font) { this.font = font; return this; }

    public String getText() { return text; }
    public TextRenderable setText(String text) { this.text = text; return this; }

    public int getFontSize() { return fontSize; }
    public TextRenderable setFontSize(int fontSize) { this.fontSize = fontSize; return this; }
    //</editor-fold>

    //<editor-fold desc="Fluent Setters Override for Method Chaining">
    @Override
    public TextRenderable setPosition(Vector2 position) { super.setPosition(position); return this; }
    @Override
    public TextRenderable setPosition(float x, float y) { super.setPosition(x, y); return this; }
    @Override
    public TextRenderable setPosition(float xy) { super.setPosition(xy); return this; }

    @Override
    public TextRenderable setTint(org.pixel.commons.Color color) { super.setTint(color); return this; }

    @Override
    public TextRenderable setDepth(int depth) { super.setDepth(depth); return this; }

    @Override
    public TextRenderable setShader(org.pixel.graphics.shader.Shader shader) { super.setShader(shader); return this; }

    @Override
    public TextRenderable setShaderData(org.pixel.commons.data.DataMap shaderData) { super.setShaderData(shaderData); return this; }

    @Override
    public TextRenderable setUniform(String name, float value) { super.setUniform(name, value); return this; }

    @Override
    public TextRenderable setUniform(String name, int value) { super.setUniform(name, value); return this; }

    @Override
    public TextRenderable setUniform(String name, Vector2 value) { super.setUniform(name, value); return this; }
    //</editor-fold>
}
