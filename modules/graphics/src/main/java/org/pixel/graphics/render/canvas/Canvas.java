/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import org.pixel.commons.lifecycle.Disposable;
import org.pixel.content.Texture;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.math.Matrix4;

/**
 * High-level fluent Canvas API for 2D drawing.
 * 
 * <p>This class provides a modern, ergonomic API for drawing shapes, text, and paths
 * using method chaining. It wraps a {@link CanvasRenderer} internally but exposes
 * only the fluent interface to users.
 * 
 * <p>Design philosophy:
 * <ul>
 *   <li><b>Fluent API</b>: All drawing operations use method chaining with {@code withX()} setters</li>
 *   <li><b>Auto-execution</b>: Last setter automatically triggers rendering (no terminal {@code .apply()} needed)</li>
 *   <li><b>Zero-GC</b>: Object pooling for all builders to minimize garbage collection</li>
 *   <li><b>Separation of concerns</b>: Canvas provides API, CanvasRenderer provides rendering</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 * Canvas canvas = new GlCanvas(width, height);
 * 
 * canvas.begin();
 * 
 * // Simple rectangle
 * canvas.rect(100, 100, 200, 50)
 *     .withFill(Color.BLUE)
 *     .withStroke(2, Color.WHITE);
 * 
 * // Circle with radial gradient
 * canvas.circle(150, 150, 30)
 *     .withFillRadialGradient(Color.YELLOW, Color.RED)
 *     .withStroke(2, Color.BLACK);
 * 
 * // Text with shadow
 * canvas.text("Hello", font, 10, 10)
 *     .withFill(Color.BLACK)
 *     .withShadow(Color.GRAY, 2, 2, 0.5f);
 * 
 * // Custom path
 * canvas.path()
 *     .moveTo(x1, y1)
 *     .lineTo(x2, y2)
 *     .closePath()
 *     .withFill(Color.GREEN);
 * 
 * canvas.end();
 * </pre>
 * 
 * <p>For advanced use cases or direct control, the underlying {@link CanvasRenderer}
 * can still be accessed via {@link #getRenderer()}.
 */
public abstract class Canvas implements Disposable {

    /** The underlying renderer that performs actual drawing operations */
    protected final CanvasRenderer renderer;
    
    // === Fluent API Builders (pooled for zero-GC) ===
    
    /** Pooled rectangle builder */
    protected final RectDrawOp rectOp;
    
    /** Pooled circle builder */
    protected final CircleDrawOp circleOp;
    
    /** Pooled line builder */
    protected final LineDrawOp lineOp;
    
    /** Pooled point builder */
    protected final PointDrawOp pointOp;
    
    /** Pooled text builder */
    protected final TextDrawOp textOp;
    
    /** Pooled path builder */
    protected final PathDrawOp pathOp;
    
    /** Pooled image builder */
    protected final ImageDrawOp imageOp;
    
    /**
     * Constructor.
     * 
     * @param renderer The underlying renderer to use for drawing operations
     */
    protected Canvas(CanvasRenderer renderer) {
        this.renderer = renderer;
        
        // Initialize pooled builders
        this.rectOp = new RectDrawOp(renderer);
        this.circleOp = new CircleDrawOp(renderer);
        this.lineOp = new LineDrawOp(renderer);
        this.pointOp = new PointDrawOp(renderer);
        this.textOp = new TextDrawOp(renderer);
        this.pathOp = new PathDrawOp(renderer);
        this.imageOp = new ImageDrawOp(renderer);
    }
    
    // === Frame Management ===
    
    /**
     * Begin a new rendering frame with screen-space coordinates.
     * Uses an orthographic projection matching the viewport dimensions.
     * Must be called before any draw operations.
     */
    public void begin() {
        renderer.begin();
    }
    
    /**
     * Begin a new rendering frame with a custom view matrix.
     * Allows rendering in world-space using a camera's view-projection matrix.
     * Must be called before any draw operations.
     * 
     * @param viewMatrix The view-projection matrix to use for this frame
     */
    public void begin(Matrix4 viewMatrix) {
        renderer.begin(viewMatrix);
    }
    
    /**
     * End the current rendering frame.
     * Flushes all pending draw calls.
     */
    public void end() {
        renderer.end();
    }
    
    // === State Management ===
    
    /**
     * Save the current rendering state (transform, clip, etc.) to a stack.
     */
    public void save() {
        renderer.save();
    }
    
    /**
     * Restore the most recently saved rendering state.
     */
    public void restore() {
        renderer.restore();
    }
    
    // === Transforms ===
    
    /**
     * Translate the coordinate system.
     */
    public void translate(float x, float y) {
        renderer.translate(x, y);
    }
    
    /**
     * Rotate the coordinate system (in radians).
     */
    public void rotate(float angle) {
        renderer.rotate(angle);
    }
    
    /**
     * Scale the coordinate system.
     */
    public void scale(float x, float y) {
        renderer.scale(x, y);
    }
    
    /**
     * Scale the coordinate system uniformly.
     */
    public void scale(float scale) {
        renderer.scale(scale);
    }
    
    // === Clipping ===
    
    /**
     * Set a rectangular clipping region.
     * Only content inside this region will be rendered.
     */
    public void clipRect(float x, float y, float width, float height) {
        renderer.clipRect(x, y, width, height);
    }
    
    /**
     * Reset clipping region to full viewport.
     */
    public void resetClip() {
        renderer.resetClip();
    }
    
    // === Fluent API Entry Points ===
    
    /**
     * Begin drawing a rectangle with fluent API.
     * 
     * <p>Example:
     * <pre>
     * canvas.rect(x, y, width, height)
     *     .withFill(Color.BLUE)
     *     .withStroke(2, Color.BLACK)
     *     .withRoundedCorners(8);
     * </pre>
     * 
     * @param x      X coordinate
     * @param y      Y coordinate
     * @param width  Width
     * @param height Height
     * @return Rectangle builder for chaining
     */
    public RectDrawOp rect(float x, float y, float width, float height) {
        return rectOp.reset().setPosition(x, y, width, height);
    }
    
    /**
     * Begin drawing a circle with fluent API.
     * 
     * <p>Example:
     * <pre>
     * canvas.circle(centerX, centerY, radius)
     *     .withFill(Color.RED)
     *     .withStroke(2, Color.BLACK);
     * </pre>
     * 
     * @param centerX Center X coordinate
     * @param centerY Center Y coordinate
     * @param radius  Radius
     * @return Circle builder for chaining
     */
    public CircleDrawOp circle(float centerX, float centerY, float radius) {
        return circleOp.reset().setPosition(centerX, centerY, radius);
    }
    
    /**
     * Begin drawing a line with fluent API.
     * 
     * <p>Example:
     * <pre>
     * canvas.line(x1, y1, x2, y2)
     *     .withStroke(2, Color.BLACK);
     * </pre>
     * 
     * @param x1 Start X coordinate
     * @param y1 Start Y coordinate
     * @param x2 End X coordinate
     * @param y2 End Y coordinate
     * @return Line builder for chaining
     */
    public LineDrawOp line(float x1, float y1, float x2, float y2) {
        return lineOp.reset().setPoints(x1, y1, x2, y2);
    }
    
    /**
     * Begin drawing a point with fluent API.
     * 
     * <p>Example:
     * <pre>
     * canvas.point(x, y)
     *     .withSize(5)
     *     .withFill(Color.BLACK);
     * </pre>
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @return Point builder for chaining
     */
    public PointDrawOp point(float x, float y) {
        return pointOp.reset().setPosition(x, y);
    }
    
    /**
     * Begin drawing text with fluent API.
     * 
     * <p>Example:
     * <pre>
     * canvas.text("Hello", font, x, y)
     *     .withFill(Color.BLACK)
     *     .withAlign(TextAlign.CENTER)
     *     .withShadow(Color.GRAY, 2, 2, 0.5f);
     * </pre>
     * 
     * @param text The text to draw
     * @param font The font to use
     * @param x    X coordinate
     * @param y    Y coordinate
     * @return Text builder for chaining
     */
    public TextDrawOp text(String text, SdfFont font, float x, float y) {
        return textOp.reset().setText(text, font, x, y);
    }
    
    /**
     * Draw an image/texture at the specified position with its original size.
     * 
     * <p>Example:
     * <pre>
     * canvas.image(myTexture, 100, 100)
     *     .withTint(Color.RED)
     *     .withAlpha(0.5f);
     * </pre>
     * 
     * @param texture The texture to draw
     * @param x       X coordinate
     * @param y       Y coordinate
     * @return Image builder for chaining
     */
    public ImageDrawOp image(Texture texture, float x, float y) {
        return imageOp.reset().setImage(texture, x, y, texture.getWidth(), texture.getHeight());
    }
    
    /**
     * Draw an image/texture at the specified position and size.
     * 
     * <p>Example:
     * <pre>
     * canvas.image(myTexture, 100, 100, 200, 150)
     *     .withSource(0, 0, 100, 100)  // Use only top-left quarter
     *     .withTint(Color.WHITE);
     * </pre>
     * 
     * @param texture The texture to draw
     * @param x       X coordinate
     * @param y       Y coordinate
     * @param width   Width to draw
     * @param height  Height to draw
     * @return Image builder for chaining
     */
    public ImageDrawOp image(Texture texture, float x, float y, float width, float height) {
        return imageOp.reset().setImage(texture, x, y, width, height);
    }
    
    /**
     * Draw a 9-patch image at the specified position and size.
     * 
     * <p>9-patches are textures that scale intelligently by dividing the image into 9 regions:
     * corners (fixed size), edges (stretch in one direction), and center (stretches both ways).
     * 
     * <p>Example:
     * <pre>
     * NinePatch panel = new NinePatch(panelTexture, 8, 8, 8, 8);
     * canvas.ninePatch(panel, 100, 100, 300, 200);
     * </pre>
     * 
     * @param ninePatch The 9-patch to draw
     * @param x         X coordinate
     * @param y         Y coordinate
     * @param width     Width to draw
     * @param height    Height to draw
     */
    public void ninePatch(org.pixel.graphics.render.NinePatch ninePatch, float x, float y, float width, float height) {
        renderer.drawNinePatch(ninePatch, x, y, width, height);
    }
    
    /**
     * Draw a 9-patch image at the specified rectangle.
     * 
     * @param ninePatch   The 9-patch to draw
     * @param destination Destination rectangle
     */
    public void ninePatch(org.pixel.graphics.render.NinePatch ninePatch, org.pixel.math.Rectangle destination) {
        renderer.drawNinePatch(ninePatch, destination);
    }
    
    /**
     * Begin drawing a path with fluent API.
     * 
     * <p>Example:
     * <pre>
     * canvas.path()
     *     .moveTo(x1, y1)
     *     .lineTo(x2, y2)
     *     .lineTo(x3, y3)
     *     .closePath()
     *     .withFill(Color.BLUE);
     * </pre>
     * 
     * @return Path builder for chaining
     */
    public PathDrawOp path() {
        return pathOp.reset().beginPath();
    }
    
    // === Text Helper Methods ===
    
    /**
     * Calculate X coordinate to horizontally center text within a rectangular area.
     * 
     * @param areaX     Left X coordinate of the area
     * @param areaWidth Width of the area
     * @param text      The text to measure
     * @param font      Font being used for the text
     * @return X coordinate to pass to text() for horizontal centering
     */
    public float centerTextHorizontally(float areaX, float areaWidth, String text, SdfFont font) {
        return renderer.centerTextHorizontally(areaX, areaWidth, text, font);
    }
    
    /**
     * Calculate Y coordinate to vertically center text within a rectangular area.
     * 
     * @param areaY      Top Y coordinate of the area
     * @param areaHeight Height of the area
     * @param font       Font being used for the text
     * @return Y coordinate to pass to text() for vertical centering
     */
    public float centerTextVertically(float areaY, float areaHeight, SdfFont font) {
        return renderer.centerTextVertically(areaY, areaHeight, font);
    }
    
    // === Advanced Access ===
    
    /**
     * Get the underlying renderer for advanced use cases.
     * 
     * <p>This allows direct access to the low-level rendering API for cases
     * where the fluent API is not sufficient or when maximum performance is needed.
     * 
     * @return The underlying CanvasRenderer
     */
    public CanvasRenderer getRenderer() {
        return renderer;
    }
    
    @Override
    public void dispose() {
        renderer.dispose();
    }
}
