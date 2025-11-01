/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import org.pixel.commons.Color;
import org.pixel.graphics.render.canvas.text.SdfFont;

/**
 * Fluent builder for text draw operations.
 * Supports fill, stroke, shadow, alignment, spacing, and full TextStyle objects.
 * 
 * <p>Usage:
 * <pre>
 * canvas.text("Hello", font, x, y)
 *     .withFill(Color.WHITE)
 *     .withStroke(Color.BLACK, 2);
 * </pre>
 */
public class TextDrawOp extends DrawOp<TextDrawOp> {
    
    // Text content and font
    private String text;
    private SdfFont font;
    private float x, y;
    
    // Style properties
    private TextStyle style;
    private boolean useStyleObject = false;
    
    // Individual properties (when not using TextStyle object)
    private Color fillColor;
    private Color strokeColor;
    private float strokeWidth;
    private Color shadowColor;
    private float shadowOffsetX, shadowOffsetY;
    private TextAlign align;
    private float letterSpacing;
    private float lineSpacing;
    private float fontSize = -1;  // -1 means use font's base size
    
    // State
    private boolean hasFill = false;
    private boolean hasStroke = false;
    private boolean hasShadow = false;
    
    /**
     * Constructor.
     * 
     * @param canvas The canvas renderer
     */
    public TextDrawOp(CanvasRenderer canvas) {
        super(canvas);
    }
    
    @Override
    protected TextDrawOp reset() {
        super.reset();
        useStyleObject = false;
        hasFill = false;
        hasStroke = false;
        hasShadow = false;
        fillColor = null;
        strokeColor = null;
        shadowColor = null;
        align = null;
        letterSpacing = 0;
        lineSpacing = 0;
        strokeWidth = 0;
        fontSize = -1;
        return this;
    }
    
    /**
     * Set text content (internal, called by canvas).
     */
    TextDrawOp setText(String text, SdfFont font, float x, float y) {
        this.text = text;
        this.font = font;
        this.x = x;
        this.y = y;
        return this;
    }
    
    /**
     * Use a complete TextStyle object.
     * This will override any individual properties set.
     * 
     * @param style Text style
     * @return This builder for chaining
     */
    public TextDrawOp withStyle(TextStyle style) {
        this.style = style;
        this.useStyleObject = true;
        return this;
    }
    
    /**
     * Set fill color.
     * 
     * @param color Fill color
     * @return This builder for chaining
     */
    public TextDrawOp withFill(Color color) {
        this.fillColor = color;
        this.hasFill = true;

        return this;
    }
    
    /**
     * Set stroke outline.
     * 
     * @param color Stroke color
     * @param width Stroke width
     * @return This builder for chaining
     */
    public TextDrawOp withStroke(Color color, float width) {
        this.strokeColor = color;
        this.strokeWidth = width;
        this.hasStroke = true;
        return this;
    }
    
    /**
     * Set drop shadow.
     * 
     * @param color   Shadow color
     * @param offsetX Shadow X offset
     * @param offsetY Shadow Y offset
     * @param alpha   Shadow opacity (0-1) - applied to shadow color
     * @return This builder for chaining
     */
    public TextDrawOp withShadow(Color color, float offsetX, float offsetY, float alpha) {
        // Apply alpha to the color
        this.shadowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        this.shadowOffsetX = offsetX;
        this.shadowOffsetY = offsetY;
        this.hasShadow = true;
        return this;
    }
    
    /**
     * Set text alignment.
     * 
     * @param align Text alignment
     * @return This builder for chaining
     */
    public TextDrawOp withAlign(TextAlign align) {
        this.align = align;
        return this;
    }
    
    /**
     * Set letter spacing.
     * 
     * @param spacing Letter spacing in pixels
     * @return This builder for chaining
     */
    public TextDrawOp withLetterSpacing(float spacing) {
        this.letterSpacing = spacing;
        return this;
    }
    
    /**
     * Set line spacing.
     * 
     * @param spacing Line spacing in pixels
     * @return This builder for chaining
     */
    public TextDrawOp withLineSpacing(float spacing) {
        this.lineSpacing = spacing;
        return this;
    }
    
    /**
     * Set font size for rendering.
     * If not set (or set to -1), uses the font's base size.
     * Internally scales the SDF font to achieve the requested size.
     * 
     * <p>Example:
     * <pre>
     * canvas.text("Hello", font, x, y)
     *     .withSize(24f)  // Render at 24px regardless of font's base size
     *     .withFill(Color.WHITE);
     * </pre>
     * 
     * @param size Font size in pixels (or -1 to use font's base size)
     * @return This builder for chaining
     */
    public TextDrawOp withSize(float size) {
        this.fontSize = size;
        return this;
    }
    
    /**
     * Get the requested font size.
     * 
     * @return Font size in pixels, or -1 if using font's base size
     */
    public float getFontSize() {
        return fontSize;
    }
    
    @Override
    protected boolean isReadyToExecute() {
        // Need either a TextStyle object or at least a fill color
        return useStyleObject || hasFill;
    }
    
    @Override
    protected void performDraw() {
        if (useStyleObject) {
            // Use the complete style object
            canvas.drawText(text, font, x, y, style, fontSize);
        } else {
            // Build a TextStyle from individual properties
            TextStyle builtStyle = new TextStyle(fillColor != null ? fillColor : Color.WHITE);
            
            if (hasStroke) {
                builtStyle.withStroke(strokeColor, strokeWidth);
            }
            
            if (hasShadow) {
                builtStyle.withShadow(shadowOffsetX, shadowOffsetY, shadowColor);
            }
            
            if (align != null) {
                builtStyle.withAlign(align);
            }
            
            if (letterSpacing != 0) {
                builtStyle.withLetterSpacing(letterSpacing);
            }
            
            if (lineSpacing != 0) {
                builtStyle.withLineSpacing(lineSpacing);
            }
            
            canvas.drawText(text, font, x, y, builtStyle, fontSize);
        }
    }
}
