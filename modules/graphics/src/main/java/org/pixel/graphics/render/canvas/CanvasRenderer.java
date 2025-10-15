/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import org.pixel.commons.Color;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Size;
import org.pixel.math.Vector2;

/**
 * Canvas-style 2D renderer with immediate-mode API.
 * Provides high-level drawing operations for shapes, text, and images.
 * 
 * <p>Design goals:
 * <ul>
 *   <li>Simple, Canvas2D-like API</li>
 *   <li>Backend-agnostic (OpenGL, Vulkan, etc.)</li>
 *   <li>Batched rendering under the hood</li>
 *   <li>First-class text rendering with SDF</li>
 * </ul>
 */
public abstract class CanvasRenderer implements Disposable {

    /**
     * Begin a new rendering frame with screen-space coordinates.
     * Uses an orthographic projection matching the viewport dimensions.
     * Must be called before any draw operations.
     */
    public abstract void begin();

    /**
     * Begin a new rendering frame with a custom view matrix.
     * Allows rendering in world-space using a camera's view-projection matrix.
     * Must be called before any draw operations.
     * 
     * @param viewMatrix The view-projection matrix to use for this frame
     */
    public abstract void begin(Matrix4 viewMatrix);

    /**
     * End the current rendering frame.
     * Flushes all pending draw calls.
     */
    public abstract void end();

    // === State Management ===

    /**
     * Save the current rendering state (transform, clip, etc.) to a stack.
     */
    public abstract void save();

    /**
     * Restore the most recently saved rendering state.
     */
    public abstract void restore();

    // === Transforms ===

    /**
     * Translate the coordinate system.
     */
    public abstract void translate(float x, float y);

    /**
     * Translate the coordinate system.
     */
    public void translate(Vector2 offset) {
        translate(offset.getX(), offset.getY());
    }

    /**
     * Rotate the coordinate system (in radians).
     */
    public abstract void rotate(float angle);

    /**
     * Scale the coordinate system.
     */
    public abstract void scale(float x, float y);

    /**
     * Scale the coordinate system uniformly.
     */
    public void scale(float scale) {
        scale(scale, scale);
    }

    // === Shapes (Filled) ===

    /**
     * Draw a filled rectangle.
     */
    public abstract void fillRect(float x, float y, float width, float height, Color color);

    /**
     * Draw a filled rectangle.
     */
    public void fillRect(Rectangle bounds, Color color) {
        fillRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), color);
    }

    /**
     * Draw a filled rounded rectangle.
     */
    public abstract void fillRoundedRect(float x, float y, float width, float height, float radius, Color color);

    /**
     * Draw a filled rounded rectangle.
     */
    public void fillRoundedRect(Rectangle bounds, float radius, Color color) {
        fillRoundedRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), radius, color);
    }

    /**
     * Draw a filled circle.
     */
    public abstract void fillCircle(float x, float y, float radius, Color color);

    /**
     * Draw a filled circle.
     */
    public void fillCircle(Vector2 center, float radius, Color color) {
        fillCircle(center.getX(), center.getY(), radius, color);
    }

    // === Shapes (Stroked) ===

    /**
     * Draw a stroked rectangle outline.
     */
    public abstract void strokeRect(float x, float y, float width, float height, float lineWidth, Color color);

    /**
     * Draw a stroked rectangle outline.
     */
    public void strokeRect(Rectangle bounds, float lineWidth, Color color) {
        strokeRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), lineWidth, color);
    }

    /**
     * Draw a stroked rounded rectangle outline.
     */
    public abstract void strokeRoundedRect(float x, float y, float width, float height, float radius, float lineWidth, Color color);

    /**
     * Draw a stroked rounded rectangle outline.
     */
    public void strokeRoundedRect(Rectangle bounds, float radius, float lineWidth, Color color) {
        strokeRoundedRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), radius, lineWidth, color);
    }

    /**
     * Draw a stroked circle outline.
     */
    public abstract void strokeCircle(float x, float y, float radius, float lineWidth, Color color);

    /**
     * Draw a stroked circle outline.
     */
    public void strokeCircle(Vector2 center, float radius, float lineWidth, Color color) {
        strokeCircle(center.getX(), center.getY(), radius, lineWidth, color);
    }

    // === Lines ===

    /**
     * Draw a line from point (x1, y1) to point (x2, y2).
     * Similar to HTML5 Canvas strokeLine() / lineTo() functionality.
     */
    public abstract void strokeLine(float x1, float y1, float x2, float y2, float lineWidth, Color color);

    /**
     * Draw a line between two points.
     */
    public void strokeLine(Vector2 start, Vector2 end, float lineWidth, Color color) {
        strokeLine(start.getX(), start.getY(), end.getX(), end.getY(), lineWidth, color);
    }

    // === Points ===

    /**
     * Draw a point (filled circle) at the specified position.
     * Similar to HTML5 Canvas API point drawing.
     */
    public abstract void fillPoint(float x, float y, float size, Color color);

    /**
     * Draw a point at the specified position.
     */
    public void fillPoint(Vector2 position, float size, Color color) {
        fillPoint(position.getX(), position.getY(), size, color);
    }

    // === Text Rendering ===

    /**
     * Draw text with simple styling.
     */
    public abstract void drawText(String text, SdfFont font, float x, float y, Color color);

    /**
     * Draw text with advanced styling (stroke, shadow, etc.).
     */
    public abstract void drawText(String text, SdfFont font, float x, float y, TextStyle style);

    /**
     * Measure text bounds.
     * 
     * @return The size of the text when rendered
     */
    public abstract Size measureText(String text, SdfFont font);

    // === Text Helper Methods ===

    /**
     * Calculate Y coordinate to vertically center text within a rectangular area.
     * 
     * <p>Since drawText() treats Y as the TOP of the text, this helper calculates
     * the correct Y value to center text within a given height.
     * 
     * @param areaY      Top Y coordinate of the area
     * @param areaHeight Height of the area
     * @param font       Font being used for the text
     * @return Y coordinate to pass to drawText() for vertical centering
     */
    public float centerTextVertically(float areaY, float areaHeight, SdfFont font) {
        return areaY + (areaHeight - font.getFontSize()) / 2;
    }

    /**
     * Calculate Y coordinate to align text baseline with a specific Y position.
     * 
     * <p>Since drawText() treats Y as the TOP of the text, this converts a baseline
     * Y coordinate to the appropriate top Y coordinate.
     * 
     * @param baselineY Y coordinate of the desired baseline
     * @param font      Font being used for the text
     * @return Y coordinate to pass to drawText()
     */
    public float textYFromBaseline(float baselineY, SdfFont font) {
        return baselineY - font.getAscent();
    }

    /**
     * Calculate X coordinate to horizontally center text within a rectangular area.
     * 
     * @param areaX     Left X coordinate of the area
     * @param areaWidth Width of the area
     * @param text      The text to measure
     * @param font      Font being used for the text
     * @return X coordinate to pass to drawText() for horizontal centering
     */
    public float centerTextHorizontally(float areaX, float areaWidth, String text, SdfFont font) {
        Size textSize = measureText(text, font);
        return areaX + (areaWidth - textSize.getWidth()) / 2;
    }

    /**
     * Calculate X and Y coordinates to center text both horizontally and vertically within a rectangular area.
     * 
     * @param areaX      Left X coordinate of the area
     * @param areaY      Top Y coordinate of the area
     * @param areaWidth  Width of the area
     * @param areaHeight Height of the area
     * @param text       The text to measure
     * @param font       Font being used for the text
     * @return Vector2 containing the centered X and Y coordinates
     */
    public Vector2 centerText(float areaX, float areaY, float areaWidth, float areaHeight, 
                             String text, SdfFont font) {
        float x = centerTextHorizontally(areaX, areaWidth, text, font);
        float y = centerTextVertically(areaY, areaHeight, font);
        return new Vector2(x, y);
    }

    // === Path API (HTML5 Canvas-like) ===

    /**
     * Begin a new path.
     * Similar to HTML5 Canvas beginPath().
     * Clears any existing path and starts a new one.
     */
    public abstract void beginPath();

    /**
     * Move the path cursor to a point without drawing.
     * Similar to HTML5 Canvas moveTo().
     * 
     * @param x X coordinate
     * @param y Y coordinate
     */
    public abstract void moveTo(float x, float y);

    /**
     * Add a line segment from the current position to the specified point.
     * Similar to HTML5 Canvas lineTo().
     * 
     * @param x X coordinate
     * @param y Y coordinate
     */
    public abstract void lineTo(float x, float y);

    /**
     * Close the current path by drawing a line back to the starting point.
     * Similar to HTML5 Canvas closePath().
     */
    public abstract void closePath();

    /**
     * Fill the current path with the specified color.
     * Similar to HTML5 Canvas fill().
     * The path is triangulated and rendered as filled polygons.
     * 
     * @param color Fill color
     */
    public abstract void fill(Color color);

    /**
     * Stroke the current path with the specified color and line width.
     * Similar to HTML5 Canvas stroke().
     * 
     * @param color     Stroke color
     * @param lineWidth Line width
     */
    public abstract void stroke(Color color, float lineWidth);

    // === Clipping ===

    /**
     * Set a rectangular clipping region.
     * Only content inside this region will be rendered.
     */
    public abstract void clipRect(float x, float y, float width, float height);

    /**
     * Set a rectangular clipping region.
     */
    public void clipRect(Rectangle bounds) {
        clipRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    /**
     * Reset clipping region to full viewport.
     */
    public abstract void resetClip();
}
