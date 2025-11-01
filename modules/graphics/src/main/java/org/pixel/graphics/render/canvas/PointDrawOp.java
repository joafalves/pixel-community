/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import org.pixel.commons.Color;

/**
 * Fluent builder for point draw operations.
 * Points are small filled circles, optionally with stroke.
 * 
 * <p>Usage:
 * <pre>
 * canvas.point(x, y)
 *     .withSize(5)
 *     .withFill(Color.RED);
 * </pre>
 */
public class PointDrawOp extends DrawOp<PointDrawOp> {
    
    // Position
    private float x, y;
    
    // Size (required)
    private boolean hasSize = false;
    private float size;
    
    // Fill
    private boolean hasFill = false;
    private Color fillColor;
    
    // Stroke (optional)
    private boolean hasStroke = false;
    private float strokeWidth;
    private Color strokeColor;
    
    /**
     * Constructor.
     * 
     * @param canvas The canvas renderer
     */
    public PointDrawOp(CanvasRenderer canvas) {
        super(canvas);
    }
    
    @Override
    protected PointDrawOp reset() {
        super.reset();
        hasSize = false;
        hasFill = false;
        hasStroke = false;
        fillColor = null;
        strokeColor = null;
        return this;
    }
    
    /**
     * Set point position (internal, called by canvas).
     */
    PointDrawOp setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    /**
     * Set point size (diameter).
     * 
     * @param size Point size
     * @return This builder for chaining
     */
    public PointDrawOp withSize(float size) {
        this.size = size;
        this.hasSize = true;
        // Don't execute yet - need fill color too
        return this;
    }
    
    /**
     * Set fill color.
     * 
     * @param color Fill color
     * @return This builder for chaining
     */
    public PointDrawOp withFill(Color color) {
        this.fillColor = color;
        this.hasFill = true;

        return this;
    }
    
    /**
     * Set stroke outline.
     * 
     * @param width Stroke width
     * @param color Stroke color
     * @return This builder for chaining
     */
    public PointDrawOp withStroke(float width, Color color) {
        this.strokeWidth = width;
        this.strokeColor = color;
        this.hasStroke = true;

        return this;
    }
    
    @Override
    protected boolean isReadyToExecute() {
        // Need size and fill at minimum
        return hasSize && hasFill;
    }
    
    @Override
    protected void performDraw() {
        // Points are implemented as small filled circles
        // Draw fill
        canvas.fillPoint(x, y, size, fillColor);
        
        // Draw stroke if specified (as stroked circle)
        if (hasStroke) {
            canvas.strokeCircle(x, y, size / 2, strokeWidth, strokeColor);
        }
    }
}
