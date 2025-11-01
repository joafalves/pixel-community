/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import org.pixel.commons.Color;

/**
 * Fluent builder for circle draw operations.
 * Supports fill, stroke, and radial gradients.
 * 
 * <p>Usage:
 * <pre>
 * canvas.circle(x, y, radius)
 *     .withFill(Color.BLUE)
 *     .withStroke(2, Color.WHITE);
 * </pre>
 */
public class CircleDrawOp extends DrawOp<CircleDrawOp> {
    
    // Position and size
    private float x, y, radius;
    
    // Fill
    private boolean hasFill = false;
    private boolean hasFillRadialGradient = false;
    private Color fillColor;
    private Color gradientCenter, gradientEdge;
    
    // Stroke
    private boolean hasStroke = false;
    private float strokeWidth;
    private Color strokeColor;
    
    /**
     * Constructor.
     * 
     * @param canvas The canvas renderer
     */
    public CircleDrawOp(CanvasRenderer canvas) {
        super(canvas);
    }
    
    /**
     * Set the circle position and radius.
     * Called internally by canvas.circle().
     * 
     * @param x      Center X coordinate
     * @param y      Center Y coordinate
     * @param radius Radius
     * @return This builder for chaining
     */
    CircleDrawOp setPosition(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        return this;
    }
    
    @Override
    protected CircleDrawOp reset() {
        super.reset();
        hasFill = false;
        hasFillRadialGradient = false;
        hasStroke = false;
        fillColor = null;
        gradientCenter = gradientEdge = null;
        strokeColor = null;
        return this;
    }
    
    /**
     * Set circle bounds (internal, called by canvas).
     */
    CircleDrawOp setBounds(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        return this;
    }
    
    /**
     * Set solid fill color.
     * 
     * @param color Fill color
     * @return This builder for chaining
     */
    public CircleDrawOp withFill(Color color) {
        this.fillColor = color;
        this.hasFill = true;
        this.hasFillRadialGradient = false;

        return this;
    }
    
    /**
     * Set radial gradient fill from center to edge.
     * 
     * @param centerColor Color at the center
     * @param edgeColor   Color at the edge
     * @return This builder for chaining
     */
    public CircleDrawOp withFillRadialGradient(Color centerColor, Color edgeColor) {
        this.gradientCenter = centerColor;
        this.gradientEdge = edgeColor;
        this.hasFill = true;
        this.hasFillRadialGradient = true;

        return this;
    }
    
    /**
     * Set stroke outline.
     * 
     * @param width Stroke width
     * @param color Stroke color
     * @return This builder for chaining
     */
    public CircleDrawOp withStroke(float width, Color color) {
        this.strokeWidth = width;
        this.strokeColor = color;
        this.hasStroke = true;

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
            if (hasFillRadialGradient) {
                canvas.fillCircleRadialGradient(x, y, radius, gradientCenter, gradientEdge);
            } else {
                canvas.fillCircle(x, y, radius, fillColor);
            }
        }
        
        // Draw stroke second (if any)
        if (hasStroke) {
            canvas.strokeCircle(x, y, radius, strokeWidth, strokeColor);
        }
    }
}
