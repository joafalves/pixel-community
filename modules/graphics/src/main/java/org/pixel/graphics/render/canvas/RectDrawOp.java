/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import org.pixel.commons.Color;

/**
 * Fluent builder for rectangle draw operations.
 * Supports fill, stroke, rounded corners, and gradients.
 * 
 * <p>Usage:
 * <pre>
 * canvas.rect(x, y, w, h)
 *     .withFill(Color.BLUE)
 *     .withStroke(2, Color.WHITE)
 *     .withRoundedCorners(8);
 * </pre>
 */
public class RectDrawOp extends DrawOp<RectDrawOp> {
    
    // Bounds
    private float x, y, width, height;
    
    // Fill
    private boolean hasFill = false;
    private boolean hasFillGradient = false;
    private Color fillColor;
    private Color gradientTL, gradientTR, gradientBR, gradientBL; // Top-left, top-right, bottom-right, bottom-left
    
    // Stroke
    private boolean hasStroke = false;
    private float strokeWidth;
    private Color strokeColor;
    
    // Rounded corners
    private float cornerRadius = 0;
    
    /**
     * Constructor.
     * 
     * @param canvas The canvas renderer
     */
    public RectDrawOp(CanvasRenderer canvas) {
        super(canvas);
    }
    
    /**
     * Set the rectangle position and size.
     * Called internally by canvas.rect().
     * 
     * @param x      X coordinate
     * @param y      Y coordinate
     * @param width  Width
     * @param height Height
     * @return This builder for chaining
     */
    RectDrawOp setPosition(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }
    
    @Override
    protected RectDrawOp reset() {
        super.reset();
        hasFill = false;
        hasFillGradient = false;
        hasStroke = false;
        cornerRadius = 0;
        fillColor = null;
        gradientTL = gradientTR = gradientBR = gradientBL = null;
        strokeColor = null;
        return this;
    }
    
    /**
     * Set rectangle bounds (internal, called by canvas).
     */
    RectDrawOp setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }
    
    /**
     * Set solid fill color.
     * 
     * @param color Fill color
     * @return This builder for chaining
     */
    public RectDrawOp withFill(Color color) {
        this.fillColor = color;
        this.hasFill = true;
        this.hasFillGradient = false;
        return this;
    }
    
    /**
     * Set 4-corner gradient fill.
     * 
     * @param topLeft     Color at top-left corner
     * @param topRight    Color at top-right corner
     * @param bottomRight Color at bottom-right corner
     * @param bottomLeft  Color at bottom-left corner
     * @return This builder for chaining
     */
    public RectDrawOp withFillGradient(Color topLeft, Color topRight, Color bottomRight, Color bottomLeft) {
        this.gradientTL = topLeft;
        this.gradientTR = topRight;
        this.gradientBR = bottomRight;
        this.gradientBL = bottomLeft;
        this.hasFill = true;
        this.hasFillGradient = true;
        return this;
    }
    
    /**
     * Set linear gradient fill (helper method).
     * Creates a 4-corner gradient based on angle.
     * 
     * @param angle      Gradient angle in radians (0 = horizontal left-to-right, PI/2 = vertical top-to-bottom)
     * @param startColor Color at gradient start
     * @param endColor   Color at gradient end
     * @return This builder for chaining
     */
    public RectDrawOp withFillLinearGradient(float angle, Color startColor, Color endColor) {
        // Use canvas helper to calculate 4-corner colors
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        
        // Normalized corner positions relative to center (-1 to 1)
        float[][] corners = {
            {-1, -1}, // top-left
            {1, -1},  // top-right
            {1, 1},   // bottom-right
            {-1, 1}   // bottom-left
        };
        
        Color[] colors = new Color[4];
        for (int i = 0; i < 4; i++) {
            // Project corner onto gradient direction
            float projection = corners[i][0] * cos + corners[i][1] * sin;
            // Map from [-sqrt(2), sqrt(2)] to [0, 1]
            float t = (projection / (float) Math.sqrt(2)) * 0.5f + 0.5f;
            colors[i] = lerpColor(startColor, endColor, t);
        }
        
        return withFillGradient(colors[0], colors[1], colors[2], colors[3]);
    }
    
    /**
     * Set stroke properties.
     * 
     * @param width Stroke width
     * @param color Stroke color
     * @return This builder for chaining
     */
    public RectDrawOp withStroke(float width, Color color) {
        this.strokeWidth = width;
        this.strokeColor = color;
        this.hasStroke = true;
        return this;
    }
    
    /**
     * Set corner radius for rounded rectangles.
     * 
     * @param radius Corner radius
     * @return This builder for chaining
     */
    public RectDrawOp withRoundedCorners(float radius) {
        this.cornerRadius = radius;
        // Don't execute yet - this is a modifier, not a terminal property
        return this;
    }
    
    @Override
    protected boolean isReadyToExecute() {
        // Need at least fill or stroke
        return hasFill || hasStroke;
    }
    
    @Override
    protected void performDraw() {
        // Draw fill first (if any)
        if (hasFill) {
            if (hasFillGradient) {
                if (cornerRadius > 0) {
                    // TODO: Rounded rect gradient not yet supported - fall back to solid color for now
                    canvas.fillRoundedRect(x, y, width, height, cornerRadius, gradientTL);
                } else {
                    canvas.fillRectGradient(x, y, width, height, gradientTL, gradientTR, gradientBR, gradientBL);
                }
            } else {
                if (cornerRadius > 0) {
                    canvas.fillRoundedRect(x, y, width, height, cornerRadius, fillColor);
                } else {
                    canvas.fillRect(x, y, width, height, fillColor);
                }
            }
        }
        
        // Draw stroke second (if any)
        if (hasStroke) {
            if (cornerRadius > 0) {
                canvas.strokeRoundedRect(x, y, width, height, cornerRadius, strokeWidth, strokeColor);
            } else {
                canvas.strokeRect(x, y, width, height, strokeWidth, strokeColor);
            }
        }
    }
    
    /**
     * Helper to interpolate colors.
     */
    private Color lerpColor(Color a, Color b, float t) {
        return new Color(
            a.getRed() + t * (b.getRed() - a.getRed()),
            a.getGreen() + t * (b.getGreen() - a.getGreen()),
            a.getBlue() + t * (b.getBlue() - a.getBlue()),
            a.getAlpha() + t * (b.getAlpha() - a.getAlpha())
        );
    }
}
