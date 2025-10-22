/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import org.pixel.commons.Color;
import org.pixel.commons.lifecycle.Disposable;
import org.pixel.content.Texture;
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
 * 
 * <p><b>Note:</b> For a modern fluent API, see {@link Canvas} which wraps this renderer.
 * This class provides the low-level immediate-mode rendering operations.
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
    
    /**
     * Update the viewport dimensions and recalculate the default projection matrix.
     * 
     * <p>This allows the canvas to adapt to window resizes or resolution changes
     * without recreating the canvas instance.
     * 
     * <p><b>Note:</b> This should not be called between {@link #begin()} and {@link #end()}.
     * Call this before starting a new frame.
     * 
     * @param width  New viewport width
     * @param height New viewport height
     */
    public abstract void setViewport(float width, float height);

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

    // === Gradients (GPU-Accelerated) ===

    /**
     * Fill a rectangle with a 4-corner gradient.
     * Each corner can have a different color, and the GPU automatically interpolates between them.
     * This is much more efficient than drawing multiple strips.
     * 
     * @param x            Rectangle X position
     * @param y            Rectangle Y position
     * @param width        Rectangle width
     * @param height       Rectangle height
     * @param topLeft      Color at top-left corner
     * @param topRight     Color at top-right corner
     * @param bottomRight  Color at bottom-right corner
     * @param bottomLeft   Color at bottom-left corner
     */
    public abstract void fillRectGradient(float x, float y, float width, float height,
                                          Color topLeft, Color topRight,
                                          Color bottomRight, Color bottomLeft);

    /**
     * Fill a rectangle with a linear gradient at the specified angle.
     * This is a convenience method that calculates the corner colors based on the angle.
     * 
     * @param x          Rectangle X position
     * @param y          Rectangle Y position
     * @param width      Rectangle width
     * @param height     Rectangle height
     * @param angle      Gradient angle in radians (0 = horizontal left-to-right, PI/2 = vertical top-to-bottom)
     * @param startColor Color at the gradient start
     * @param endColor   Color at the gradient end
     */
    public void fillRectLinearGradient(float x, float y, float width, float height,
                                       float angle, Color startColor, Color endColor) {
        // Calculate corner colors based on angle
        // Project each corner onto the gradient direction to get its interpolation value
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
        
        fillRectGradient(x, y, width, height, colors[0], colors[1], colors[2], colors[3]);
    }

    /**
     * Fill a circle with a radial gradient from center to edge.
     * This approximates a radial gradient using a triangle fan with per-vertex colors.
     * 
     * @param centerX     Circle center X
     * @param centerY     Circle center Y
     * @param radius      Circle radius
     * @param centerColor Color at the center
     * @param edgeColor   Color at the edge
     */
    public abstract void fillCircleRadialGradient(float centerX, float centerY, float radius,
                                                  Color centerColor, Color edgeColor);

    /**
     * Helper method to linearly interpolate between two colors.
     * 
     * @param a Start color
     * @param b End color
     * @param t Interpolation factor (0 = a, 1 = b)
     * @return Interpolated color
     */
    protected Color lerpColor(Color a, Color b, float t) {
        return new Color(
            a.getRed() + t * (b.getRed() - a.getRed()),
            a.getGreen() + t * (b.getGreen() - a.getGreen()),
            a.getBlue() + t * (b.getBlue() - a.getBlue()),
            a.getAlpha() + t * (b.getAlpha() - a.getAlpha())
        );
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
     * Draw text with advanced styling (stroke, shadow, etc.) using font's base size.
     * Convenience method that calls drawText() with fontSize=-1.
     * 
     * @param text The text to draw
     * @param font The SDF font to use
     * @param x The x position
     * @param y The y position
     * @param style The text style (fill, stroke, shadow, etc.)
     */
    public void drawText(String text, SdfFont font, float x, float y, TextStyle style) {
        drawText(text, font, x, y, style, -1);
    }

    /**
     * Draw text with advanced styling (stroke, shadow, etc.) and custom size.
     * 
     * @param text The text to draw
     * @param font The SDF font to use
     * @param x The x position
     * @param y The y position
     * @param style The text style (fill, stroke, shadow, etc.)
     * @param fontSize The font size in pixels, or -1 to use font's base size
     */
    public abstract void drawText(String text, SdfFont font, float x, float y, TextStyle style, float fontSize);

    /**
     * Measure text bounds with default spacing.
     * 
     * @param text The text to measure
     * @param font The font to use
     * @return The size of the text when rendered
     */
    public abstract Size measureText(String text, SdfFont font);

    /**
     * Measure text bounds with custom text style (including letter and line spacing).
     * 
     * @param text  The text to measure
     * @param font  The font to use
     * @param style The text style (letter/line spacing will be applied)
     * @return The size of the text when rendered
     */
    public abstract Size measureText(String text, SdfFont font, TextStyle style);
    
    /**
     * Measure text bounds with custom font size (zero-GC version).
     * 
     * <p>This method measures text at a specific font size, accounting for both
     * the fontSize scale and any Canvas transform scale. This ensures measurements
     * match rendering exactly.
     * 
     * @param text     The text to measure
     * @param font     The font to use
     * @param fontSize The font size in pixels (or -1/0 to use font's base size)
     * @return The size of the text when rendered
     */
    public abstract Size measureText(String text, SdfFont font, float fontSize);

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

    // === Image/Texture Rendering ===

    /**
     * Draw a texture at the specified position with its original size.
     * 
     * @param texture The texture to draw
     * @param x       X position
     * @param y       Y position
     */
    public abstract void drawImage(Texture texture, float x, float y);

    /**
     * Draw a texture at the specified position.
     */
    public void drawImage(Texture texture, Vector2 position) {
        drawImage(texture, position.getX(), position.getY());
    }

    /**
     * Draw a texture at the specified position and size.
     * 
     * @param texture The texture to draw
     * @param x       X position
     * @param y       Y position
     * @param width   Width to draw the texture
     * @param height  Height to draw the texture
     */
    public abstract void drawImage(Texture texture, float x, float y, float width, float height);

    /**
     * Draw a texture at the specified position and size.
     */
    public void drawImage(Texture texture, Rectangle destination) {
        drawImage(texture, destination.getX(), destination.getY(), 
                 destination.getWidth(), destination.getHeight());
    }

    /**
     * Draw a portion of a texture (source rectangle) to a destination rectangle.
     * Similar to HTML5 Canvas drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh).
     * 
     * @param texture     The texture to draw
     * @param source      Source rectangle (portion of texture to draw)
     * @param destination Destination rectangle (where to draw on screen)
     */
    public abstract void drawImage(Texture texture, Rectangle source, Rectangle destination);

    /**
     * Draw a texture with tint color.
     * 
     * @param texture The texture to draw
     * @param x       X position
     * @param y       Y position
     * @param width   Width to draw the texture
     * @param height  Height to draw the texture
     * @param tint    Color to tint the texture (Color.WHITE for no tint)
     */
    public abstract void drawImage(Texture texture, float x, float y, float width, float height, Color tint);

    /**
     * Draw a texture with full control (source rect, destination rect, rotation, anchor, tint).
     * 
     * @param texture     The texture to draw
     * @param source      Source rectangle (null for full texture)
     * @param destination Destination rectangle
     * @param rotation    Rotation in radians
     * @param anchor      Anchor point for rotation (0-1 normalized, e.g., 0.5, 0.5 for center)
     * @param tint        Tint color (Color.WHITE for no tint)
     */
    public abstract void drawImage(Texture texture, Rectangle source, Rectangle destination,
                                   float rotation, Vector2 anchor, Color tint);

    /**
     * Draw a 9-patch image that scales intelligently while preserving corners and edges.
     * The 9-patch will be rendered by making 9 drawImage calls that will be batched efficiently.
     * 
     * @param ninePatch The 9-patch to draw
     * @param x         X position
     * @param y         Y position
     * @param width     Target width
     * @param height    Target height
     */
    public void drawNinePatch(org.pixel.graphics.render.NinePatch ninePatch, float x, float y, float width, float height) {
        ninePatch.render(this, x, y, width, height);
    }

    /**
     * Draw a 9-patch image at the specified rectangle.
     * 
     * @param ninePatch   The 9-patch to draw
     * @param destination Destination rectangle
     */
    public void drawNinePatch(org.pixel.graphics.render.NinePatch ninePatch, Rectangle destination) {
        drawNinePatch(ninePatch, destination.getX(), destination.getY(), 
                     destination.getWidth(), destination.getHeight());
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
