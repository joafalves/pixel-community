/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import org.pixel.commons.Color;

/**
 * Fluent builder for path draw operations.
 * Provides Canvas2D-style path API with method chaining.
 * 
 * <p>Usage:
 * <pre>
 * canvas.path()
 *     .moveTo(x1, y1)
 *     .lineTo(x2, y2)
 *     .lineTo(x3, y3)
 *     .closePath()
 *     .withFill(Color.BLUE);
 * </pre>
 * 
 * <p>Important: Order matters! Vertices are added in the order methods are called.
 */
public class PathDrawOp extends DrawOp<PathDrawOp> {
    
    private final CanvasRenderer canvas;
    
    // State
    private boolean pathStarted = false;
    private boolean hasFill = false;
    private boolean hasStroke = false;
    private Color fillColor;
    private Color strokeColor;
    private float strokeWidth;
    
    /**
     * Constructor.
     * 
     * @param canvas The canvas renderer
     */
    public PathDrawOp(CanvasRenderer canvas) {
        super(canvas);
        this.canvas = canvas;
    }
    
    @Override
    protected PathDrawOp reset() {
        super.reset();
        pathStarted = false;
        hasFill = false;
        hasStroke = false;
        fillColor = null;
        strokeColor = null;
        return this;
    }
    
    /**
     * Begin a new path.
     * This is called automatically by canvas.path(), but can be called again to start over.
     * 
     * @return This builder for chaining
     */
    public PathDrawOp beginPath() {
        canvas.beginPath();
        pathStarted = true;
        return this;
    }
    
    /**
     * Move path cursor to a point without drawing.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @return This builder for chaining
     */
    public PathDrawOp moveTo(float x, float y) {
        if (!pathStarted) {
            beginPath();
        }
        canvas.moveTo(x, y);
        return this;
    }
    
    /**
     * Add a line segment from current position to specified point.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @return This builder for chaining
     */
    public PathDrawOp lineTo(float x, float y) {
        if (!pathStarted) {
            throw new IllegalStateException("Path not started. Call moveTo() first.");
        }
        canvas.lineTo(x, y);
        return this;
    }
    
    /**
     * Close the path by drawing a line back to the starting point.
     * 
     * @return This builder for chaining
     */
    public PathDrawOp closePath() {
        if (!pathStarted) {
            throw new IllegalStateException("Path not started. Call moveTo() first.");
        }
        canvas.closePath();
        return this;
    }
    
    /**
     * Fill the path with a color.
     * This executes the draw operation.
     * 
     * @param color Fill color
     * @return This builder for chaining
     */
    public PathDrawOp withFill(Color color) {
        this.fillColor = color;
        this.hasFill = true;

        return this;
    }
    
    /**
     * Stroke the path outline.
     * This executes the draw operation.
     * 
     * @param width Stroke width
     * @param color Stroke color
     * @return This builder for chaining
     */
    public PathDrawOp withStroke(float width, Color color) {
        this.strokeWidth = width;
        this.strokeColor = color;
        this.hasStroke = true;

        return this;
    }
    
    @Override
    protected boolean isReadyToExecute() {
        // Need path started and either fill or stroke
        return pathStarted && (hasFill || hasStroke);
    }
    
    @Override
    protected void performDraw() {
        // Draw fill first (if any)
        if (hasFill) {
            canvas.fill(fillColor);
        }
        
        // Draw stroke second (if any)
        if (hasStroke) {
            canvas.stroke(strokeColor, strokeWidth);
        }
    }
}
