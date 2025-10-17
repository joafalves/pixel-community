package org.pixel.graphics.render.renderable;

import org.pixel.commons.Color;
import org.pixel.graphics.render.Renderable;
import org.pixel.graphics.render.SdfTextRenderer;
import org.pixel.graphics.render.canvas.TextStyle;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

/**
 * Renderable for SDF-based text rendering.
 * Defers actual rendering until the RenderPipeline processes it.
 */
public class SdfTextRenderable extends Renderable<SdfTextRenderer> {

    private String text;
    private SdfFont font;
    private TextStyle style;
    private Matrix4 transform;

    /**
     * Constructor.
     */
    public SdfTextRenderable() {
        super(SdfTextRenderer.class);
        this.text = "";
        this.style = new TextStyle(Color.WHITE);
        this.transform = new Matrix4();
    }

    @Override
    public void render(SdfTextRenderer renderer, Matrix4 viewMatrix) {
        // Use the transform as-is (it already contains currentTransform * viewMatrix from Canvas)
        // Do NOT multiply by viewMatrix again - that would apply it twice!
        renderer.render(text, font, position.getX(), position.getY(), style, transform);
    }

    @Override
    public Rectangle getBounds() {
        if (font == null || text == null || text.isEmpty()) {
            return null;
        }

        // Estimate text bounds based on font metrics
        // TODO: Calculate actual bounds from glyph metrics
        float estimatedWidth = text.length() * font.getFontSize() * 0.6f;
        float estimatedHeight = font.getFontSize();

        return new Rectangle(position.getX(), position.getY(), estimatedWidth, estimatedHeight);
    }

    //<editor-fold desc="Text-Specific Getters and Setters">
    public String getText() { return text; }
    public SdfTextRenderable setText(String text) { this.text = text; return this; }

    public SdfFont getFont() { return font; }
    public SdfTextRenderable setFont(SdfFont font) { this.font = font; return this; }

    public TextStyle getStyle() { return style; }
    public SdfTextRenderable setStyle(TextStyle style) { this.style = style; return this; }

    public Matrix4 getTransform() { return transform; }
    public SdfTextRenderable setTransform(Matrix4 transform) { 
        this.transform = new Matrix4(transform); 
        return this; 
    }
    //</editor-fold>

    //<editor-fold desc="Fluent Setters Override for Method Chaining">
    @Override
    public SdfTextRenderable setPosition(Vector2 position) { super.setPosition(position); return this; }
    @Override
    public SdfTextRenderable setPosition(float x, float y) { super.setPosition(x, y); return this; }
    @Override
    public SdfTextRenderable setPosition(float xy) { super.setPosition(xy); return this; }

    @Override
    public SdfTextRenderable setTint(Color color) { super.setTint(color); return this; }

    @Override
    public SdfTextRenderable setDepth(int depth) { super.setDepth(depth); return this; }
    //</editor-fold>
}
