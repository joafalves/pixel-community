/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import org.pixel.commons.Color;

/**
 * Fluent builder for line draw operations.
 * 
 * <p>Usage:
 * <pre>
 * canvas.line(x1, y1, x2, y2)
 *     .withStroke(2, Color.WHITE);
 * </pre>
 */
public class LineDrawOp extends DrawOp<LineDrawOp> {
    
    // Endpoints
    private float x1, y1, x2, y2;
    
    // Stroke (required for lines)
    private boolean hasStroke = false;
    private float strokeWidth;
    private Color strokeColor;
    
    /**
     * Constructor.
     * 
     * @param canvas The canvas renderer
     */
    public LineDrawOp(CanvasRenderer canvas) {
        super(canvas);
    }
    
    @Override
    protected LineDrawOp reset() {
        super.reset();
        hasStroke = false;
        strokeColor = null;
        return this;
    }
    
    /**
     * Set line endpoints (internal, called by canvas).
     */
    LineDrawOp setPoints(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        return this;
    }
    
    /**
     * Set stroke width and color.
     * 
     * @param width Stroke width
     * @param color Stroke color
     * @return This builder for chaining
     */
    public LineDrawOp withStroke(float width, Color color) {
        this.strokeWidth = width;
        this.strokeColor = color;
        this.hasStroke = true;

        return this;
    }
    
    @Override
    protected boolean isReadyToExecute() {
        // Lines require stroke
        return hasStroke;
    }
    
    @Override
    protected void performDraw() {
        canvas.strokeLine(x1, y1, x2, y2, strokeWidth, strokeColor);
    }
}
