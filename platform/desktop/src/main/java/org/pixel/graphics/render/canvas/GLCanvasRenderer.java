/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import org.pixel.commons.Color;
import org.pixel.content.Texture;
import org.pixel.content.opengl.GLTexture;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.math.MathHelper;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Size;
import org.pixel.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * OpenGL implementation of CanvasRenderer using unified SDF batch rendering.
 * ALL drawing operations go through a single GlSdfBatchRenderer for perfect draw order.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Unified SDF-based rendering - all primitives use the same shader</li>
 *   <li>Perfect draw order - everything batched in submission order</li>
 *   <li>High performance - minimal draw calls through batching</li>
 *   <li>Multi-texture support - batches up to GL_MAX_TEXTURE_IMAGE_UNITS textures</li>
 *   <li>Transform stack (translate, rotate, scale)</li>
 *   <li>Clipping with scissor test</li>
 * </ul>
 */
public class GLCanvasRenderer extends CanvasRenderer {

    private Matrix4 activeViewMatrix; // View matrix for current frame
    private final Matrix4 defaultViewMatrix; // Default screen-space projection
    private final Stack<TransformState> transformStack;
    private final GLSdfBatchRenderer batchRenderer; // UNIFIED renderer for everything!
    private TransformState currentTransform;
    private boolean begun = false;
    
    // Multi-texture batching support
    private final int shaderTextureCount; // Maximum simultaneous textures
    private final HashMap<Integer, Integer> textureUnitMap = new HashMap<>();
    private int textureUnitCounter = 0;
    
    // Path API state
    private final List<Vector2> pathPoints = new ArrayList<>();
    private boolean pathStarted = false;
    private boolean pathClosed = false;

    /**
     * Transform state for save/restore operations.
     */
    private static class TransformState {
        Matrix4 transform;
        Rectangle clipRect;

        TransformState() {
            this.transform = new Matrix4();
            this.clipRect = null;
        }

        TransformState(TransformState copy) {
            this.transform = new Matrix4(copy.transform);
            this.clipRect = copy.clipRect != null ? new Rectangle(copy.clipRect) : null;
        }
    }

    /**
     * Constructor with default viewport dimensions for screen-space rendering.
     *
     * @param viewportWidth  Viewport width
     * @param viewportHeight Viewport height
     */
    public GLCanvasRenderer(float viewportWidth, float viewportHeight) {
        this(viewportWidth, viewportHeight, 0); // Auto-detect texture count
    }

    /**
     * Constructor with custom texture slot count.
     *
     * @param viewportWidth      Viewport width
     * @param viewportHeight     Viewport height
     * @param shaderTextureCount Maximum simultaneous textures (0 = auto-detect)
     */
    public GLCanvasRenderer(float viewportWidth, float viewportHeight, int shaderTextureCount) {
        this.defaultViewMatrix = Matrix4.orthographic(0, viewportWidth, viewportHeight, 0, -1, 1);
        this.activeViewMatrix = this.defaultViewMatrix;
        this.transformStack = new Stack<>();
        this.batchRenderer = new GLSdfBatchRenderer(); // ONE renderer for everything!
        this.currentTransform = new TransformState();
        
        // Query hardware for max texture units if not specified
        if (shaderTextureCount <= 0) {
            int[] textureUnits = new int[1];
            glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, textureUnits);
            this.shaderTextureCount = Math.max(textureUnits[0], 1);

        } else {
            this.shaderTextureCount = shaderTextureCount;
        }
    }

    @Override
    public void begin() {
        begin(defaultViewMatrix);
    }

    @Override
    public void begin(Matrix4 viewMatrix) {
        if (begun) {
            throw new IllegalStateException("CanvasRenderer.begin() called twice without end()");
        }
        
        this.activeViewMatrix = viewMatrix;
        begun = true;
        
        // Reset transform stack and current transform at the start of each frame
        transformStack.clear();
        currentTransform.transform.setIdentity();
        currentTransform.clipRect = null;
        
        // Reset texture tracking
        textureUnitMap.clear();
        textureUnitCounter = 0;
        
        // Disable scissor test at the start
        glDisable(GL_SCISSOR_TEST);
        
        // Start batch renderer with view matrix only
        // Local transforms will be applied on CPU before batching
        batchRenderer.begin(activeViewMatrix, currentTransform.transform);
    }

    @Override
    public void end() {
        if (!begun) {
            throw new IllegalStateException("CanvasRenderer.end() called without begin()");
        }
        
        // Flush all batched rendering
        batchRenderer.end();
        begun = false;
    }
    
    @Override
    public void setViewport(float width, float height) {
        if (begun) {
            throw new IllegalStateException("Cannot change viewport between begin() and end()");
        }
        
        // Update the default view matrix with new dimensions
        // Since defaultViewMatrix is modified in place, the change takes effect
        // on the next begin() call if using default view
        this.defaultViewMatrix.setOrthographic(0, width, height, 0, -1, 1);
    }

    @Override
    public void save() {
        // Push a copy of the current transform state onto the stack
        transformStack.push(new TransformState(currentTransform));
    }

    @Override
    public void restore() {
        if (transformStack.isEmpty()) {
            throw new IllegalStateException("Cannot restore() without matching save()");
        }
        
        // Pop the previous state from the stack
        TransformState previousState = transformStack.pop();
        
        // Check if clipping state changed
        boolean clipChanged = (currentTransform.clipRect == null) != (previousState.clipRect == null) ||
                             (currentTransform.clipRect != null && !currentTransform.clipRect.equals(previousState.clipRect));
        
        // Update current transform
        currentTransform = previousState;
        
        // Only flush if clipping changed
        if (clipChanged) {
            batchRenderer.end();
            applyClipping();
            batchRenderer.begin(activeViewMatrix, currentTransform.transform);
        } else {
            // Just update the transform reference - no flush needed!
            batchRenderer.setLocalTransform(currentTransform.transform);
        }
    }

    @Override
    public void translate(float x, float y) {
        // Apply translation to local transform
        currentTransform.transform.translate(x, y, 0);
        
        // Update batch renderer's transform reference - no flush needed!
        batchRenderer.setLocalTransform(currentTransform.transform);
    }

    @Override
    public void rotate(float angle) {
        // Apply rotation to local transform
        currentTransform.transform.rotate(angle, 0, 0, 1);
        
        // Update batch renderer's transform reference - no flush needed!
        batchRenderer.setLocalTransform(currentTransform.transform);
    }

    @Override
    public void scale(float x, float y) {
        // Apply scale to local transform
        currentTransform.transform.scale(x, y, 1);
        
        // Update batch renderer's transform reference - no flush needed!
        batchRenderer.setLocalTransform(currentTransform.transform);
    }

    @Override
    public void fillRect(float x, float y, float width, float height, Color color) {
        // Batched rendering - everything goes through unified renderer!
        batchRenderer.fillRoundedRect(x, y, width, height, 0, color);
    }

    @Override
    public void fillRoundedRect(float x, float y, float width, float height, float radius, Color color) {
        // Batched rendering
        batchRenderer.fillRoundedRect(x, y, width, height, radius, color);
    }

    @Override
    public void fillCircle(float x, float y, float radius, Color color) {
        // Batched rendering
        batchRenderer.fillCircle(x, y, radius, color);
    }

    @Override
    public void fillRectGradient(float x, float y, float width, float height,
                                 Color topLeft, Color topRight,
                                 Color bottomRight, Color bottomLeft) {
        // Batched gradient rendering with per-vertex colors
        batchRenderer.fillRectGradient(x, y, width, height, topLeft, topRight, bottomRight, bottomLeft);
    }

    @Override
    public void fillCircleRadialGradient(float centerX, float centerY, float radius,
                                         Color centerColor, Color edgeColor) {
        // Batched radial gradient rendering
        batchRenderer.fillCircleRadialGradient(centerX, centerY, radius, centerColor, edgeColor);
    }

    @Override
    public void strokeRect(float x, float y, float width, float height, float lineWidth, Color color) {
        // Batched rendering
        batchRenderer.strokeRoundedRect(x, y, width, height, 0, lineWidth, color);
    }

    @Override
    public void strokeRoundedRect(float x, float y, float width, float height, float radius, float lineWidth, Color color) {
        // Batched rendering
        batchRenderer.strokeRoundedRect(x, y, width, height, radius, lineWidth, color);
    }

    @Override
    public void strokeCircle(float x, float y, float radius, float lineWidth, Color color) {
        // Batched rendering
        batchRenderer.strokeCircle(x, y, radius, lineWidth, color);
    }

    @Override
    public void strokeLine(float x1, float y1, float x2, float y2, float lineWidth, Color color) {
        // Batched rendering - goes through uber shader!
        batchRenderer.strokeLine(x1, y1, x2, y2, lineWidth, color);
    }

    @Override
    public void fillPoint(float x, float y, float size, Color color) {
        // Batched rendering
        batchRenderer.fillPoint(x, y, size, color);
    }

    @Override
    public void drawText(String text, SdfFont font, float x, float y, Color color) {
        drawText(text, font, x, y, new TextStyle(color));
    }

    @Override
    public void drawText(String text, SdfFont font, float x, float y, TextStyle style) {
        if (text == null || text.isEmpty() || font == null) {
            return;
        }

        // Calculate alignment offsets
        float offsetX = 0;
        float offsetY = 0;
        
        TextAlign align = style.getAlign();
        if (align != null) {
            Size textSize = measureText(text, font, style);
            
            // Horizontal alignment
            switch (align.getHorizontal()) {
                case CENTER:
                    offsetX = -textSize.getWidth() / 2;
                    break;
                case RIGHT:
                    offsetX = -textSize.getWidth();
                    break;
                case LEFT:
                default:
                    offsetX = 0;    
                    break;
            }
            
            // Vertical alignment
            // Note: We subtract SDF_PADDING when rendering glyphs, so we need to compensate
            // for that in alignment calculations
            final float SDF_PADDING = GLSdfConstants.SDF_PADDING_PX / 2.0f;
            
            switch (align.getVertical()) {
                case MIDDLE:
                    offsetY = -textSize.getHeight() / 2 + SDF_PADDING;
                    break;
                case BASELINE:
                    // Y is already at top, move down by ascent to get to baseline
                    // Add SDF_PADDING since we subtract it during rendering
                    offsetY = font.getAscent() + SDF_PADDING;
                    break;
                case BOTTOM:
                    // For BOTTOM, we need additional offset to account for descenders
                    // The measured height is lineHeight, but visual bottom includes SDF padding
                    offsetY = -textSize.getHeight() + SDF_PADDING * 2;
                    break;
                case TOP:
                default:
                    // For TOP alignment, we need to add SDF_PADDING to compensate for the
                    // subtraction during rendering
                    offsetY = SDF_PADDING;
                    break;
            }
        }
        
        float adjustedX = x + offsetX;
        float adjustedY = y + offsetY;

        // Render drop shadow first (if enabled)
        if (style.isDropShadow()) {
            Vector2 shadowOffset = style.getShadowOffset();
            Color shadowColor = style.getShadowColor();
            
            // Shadow has no stroke, same letter/line spacing
            batchRenderer.drawText(text, font, 
                adjustedX + shadowOffset.getX(), 
                adjustedY + shadowOffset.getY(), 
                shadowColor, Color.BLACK, 0.0f, style.getLetterSpacing(), style.getLineSpacing());
        }

        // Render main text
        Color fillColor = style.getFillColor() != null ? style.getFillColor() : Color.WHITE;
        Color strokeColor = style.hasStroke() ? style.getStrokeColor() : Color.BLACK;
        float strokeWidth = style.hasStroke() ? style.getStrokeWidth() : 0.0f;
        float letterSpacing = style.getLetterSpacing();
        float lineSpacing = style.getLineSpacing();
        
        batchRenderer.drawText(text, font, adjustedX, adjustedY, fillColor, strokeColor, strokeWidth, letterSpacing, lineSpacing);
    }

    @Override
    public Size measureText(String text, SdfFont font, TextStyle style) {
        if (style == null) {
            return measureText(text, font);
        }
        return measureTextInternal(text, font, style.getLetterSpacing(), style.getLineSpacing());
    }

    @Override
    public Size measureText(String text, SdfFont font) {
        // Delegate to the TextStyle version with default values
        return measureText(text, font, new TextStyle());
    }

    /**
     * Internal method to measure text size with custom letter and line spacing.
     */
    private Size measureTextInternal(String text, SdfFont font, float letterSpacing, float lineSpacing) {
        if (text == null || text.isEmpty() || font == null) {
            return new Size(0, 0);
        }

        String[] lines = text.split("\n", -1);
        float maxWidth = 0;
        float totalHeight = 0;

        for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
            String line = lines[lineIdx];
            float lineWidth = 0;
            
            for (int i = 0; i < line.length(); i++) {
                char ch = line.charAt(i);
                
                // Handle spaces (matching the rendering logic)
                if (ch == ' ') {
                    float spaceWidth = font.getFontSize() * GLSdfConstants.SPACE_WIDTH_RATIO;
                    lineWidth += spaceWidth + letterSpacing;
                    continue;
                }
                
                var glyph = font.getGlyph(ch);
                if (glyph != null) {
                    lineWidth += glyph.getAdvance() + letterSpacing;
                }
            }
            
            maxWidth = Math.max(maxWidth, lineWidth);
            totalHeight += font.getLineHeight();
            
            if (lineIdx < lines.length - 1) {
                totalHeight += lineSpacing;
            }
        }

        // Apply current transform scale to the measured size
        float[][] mat = currentTransform.transform.toUnsafeArray();
        float scaleX = (float) Math.sqrt(mat[0][0] * mat[0][0] + mat[0][1] * mat[0][1]);
        float scaleY = (float) Math.sqrt(mat[1][0] * mat[1][0] + mat[1][1] * mat[1][1]);

        return new Size(maxWidth * scaleX, totalHeight * scaleY);
    }

    @Override
    public void clipRect(float x, float y, float width, float height) {
        // End batch before changing clipping state
        batchRenderer.end();
        
        currentTransform.clipRect = new Rectangle(x, y, width, height);
        applyClipping();
        
        // Restart batch with new clipping state
        batchRenderer.begin(activeViewMatrix, currentTransform.transform);
    }

    @Override
    public void resetClip() {
        // End batch before changing clipping state
        batchRenderer.end();
        
        currentTransform.clipRect = null;
        glDisable(GL_SCISSOR_TEST);
        
        // Restart batch with no clipping
        batchRenderer.begin(activeViewMatrix, currentTransform.transform);
    }

    /**
     * Apply the current clipping rectangle using OpenGL scissor test.
     * Note: Scissor test uses window coordinates (origin at bottom-left),
     * while our canvas uses top-left origin.
     */
    private void applyClipping() {
        if (currentTransform.clipRect == null) {
            glDisable(GL_SCISSOR_TEST);
            return;
        }

        Rectangle clip = currentTransform.clipRect;
        
        // Query actual viewport dimensions from OpenGL
        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        int actualViewportHeight = viewport[3];
        
        // Enable scissor test
        glEnable(GL_SCISSOR_TEST);
        
        // Convert from top-left origin to bottom-left origin (OpenGL convention)
        // Canvas Y=0 is top, OpenGL Y=0 is bottom
        int scissorX = (int) clip.getX();
        int scissorY = actualViewportHeight - (int) (clip.getY() + clip.getHeight());
        int scissorWidth = (int) clip.getWidth();
        int scissorHeight = (int) clip.getHeight();
        
        glScissor(scissorX, scissorY, scissorWidth, scissorHeight);
    }

    // ============================================================================
    // Image/Texture Rendering Implementation
    // ============================================================================

    @Override
    public void drawImage(Texture texture, float x, float y) {
        drawImage(texture, x, y, texture.getWidth(), texture.getHeight());
    }

    @Override
    public void drawImage(Texture texture, float x, float y, float width, float height) {
        drawImage(texture, x, y, width, height, Color.WHITE);
    }

    @Override
    public void drawImage(Texture texture, Rectangle source, Rectangle destination) {
        drawImage(texture, source, destination, 0, new Vector2(0, 0), Color.WHITE);
    }

    @Override
    public void drawImage(Texture texture, float x, float y, float width, float height, Color tint) {
        drawImage(texture, null, new Rectangle(x, y, width, height), 0, new Vector2(0, 0), tint);
    }

    @Override
    public void drawImage(Texture texture, Rectangle source, Rectangle destination,
                         float rotation, Vector2 anchor, Color tint) {
        if (texture == null || destination == null) {
            return;
        }

        int textureId = ((GLTexture) texture).getId();
        
        // Check if we need to flush early due to texture slots being full
        if (!textureUnitMap.containsKey(textureId)) {
            if (textureUnitCounter >= shaderTextureCount) {
                // Flush the batch - texture slots are full!
                updateBatchRendererTextures();
                batchRenderer.end();
                textureUnitMap.clear();
                textureUnitCounter = 0;
                batchRenderer.begin(activeViewMatrix, currentTransform.transform);
            }
            textureUnitMap.put(textureId, textureUnitCounter++);
        }
        
        int textureSlot = textureUnitMap.get(textureId);
        
        // Calculate source UVs
        float srcX, srcY, srcWidth, srcHeight;
        if (source != null) {
            srcX = source.getX() / texture.getWidth();
            srcY = source.getY() / texture.getHeight();
            srcWidth = source.getWidth() / texture.getWidth();
            srcHeight = source.getHeight() / texture.getHeight();
        } else {
            srcX = 0;
            srcY = 0;
            srcWidth = 1;
            srcHeight = 1;
        }
        
        // Handle rotation and anchor using transform stack
        boolean needsTransform = rotation != 0 || (anchor != null && (anchor.getX() != 0 || anchor.getY() != 0));
        
        if (needsTransform) {
            // Save current transform
            save();
            
            // Calculate anchor point in pixels
            float anchorX = anchor != null ? anchor.getX() : 0;
            float anchorY = anchor != null ? anchor.getY() : 0;
            float anchorPixelX = destination.getX() + destination.getWidth() * anchorX;
            float anchorPixelY = destination.getY() + destination.getHeight() * anchorY;
            
            // Apply transform: translate to anchor, rotate, translate back
            translate(anchorPixelX, anchorPixelY);
            if (rotation != 0) {
                rotate(rotation);
            }
            translate(-anchorPixelX, -anchorPixelY);
        }
        
        // Update active textures in batch renderer
        updateBatchRendererTextures();
        
        // Draw the textured quad
        batchRenderer.drawTexturedQuad(
            destination.getX(), destination.getY(),
            destination.getWidth(), destination.getHeight(),
            tint,
            srcX, srcY, srcWidth, srcHeight,
            textureSlot
        );
        
        if (needsTransform) {
            // Restore transform
            restore();
        }
    }

    /**
     * Update the batch renderer with the current active textures.
     */
    private void updateBatchRendererTextures() {
        if (textureUnitMap.isEmpty()) {
            return;
        }
        
        // Convert HashMap to sorted array
        int[] textureIds = new int[textureUnitCounter];
        for (var entry : textureUnitMap.entrySet()) {
            textureIds[entry.getValue()] = entry.getKey();
        }
        
        batchRenderer.setActiveTextures(textureIds, textureUnitCounter);
    }

    // ============================================================================
    // Path API Implementation
    // ============================================================================

    @Override
    public void beginPath() {
        pathPoints.clear();
        pathStarted = false;
        pathClosed = false;
    }

    @Override
    public void moveTo(float x, float y) {
        // Start a new sub-path at this point
        pathPoints.add(new Vector2(x, y));
        pathStarted = true;
        pathClosed = false; // Opening a new path segment
    }

    @Override
    public void lineTo(float x, float y) {
        if (!pathStarted) {
            // If no current point, treat as moveTo
            moveTo(x, y);
            return;
        }
        pathPoints.add(new Vector2(x, y));
    }

    @Override
    public void closePath() {
        if (!pathStarted || pathPoints.size() < 2 || pathClosed) {
            return; // Nothing to close
        }
        
        // Explicitly close the path by connecting last point to first point
        // This ensures the closing segment is part of the path geometry
        Vector2 first = pathPoints.get(0);
        Vector2 last = pathPoints.get(pathPoints.size() - 1);
        
        // Only add closing point if it's not already at the start position
        float dx = last.getX() - first.getX();
        float dy = last.getY() - first.getY();
        float distSq = dx * dx + dy * dy;
        
        if (distSq > 0.0001f) { // Tolerance for floating point comparison
            // Add the first point again to explicitly close the path
            pathPoints.add(new Vector2(first));
        }
        
        pathClosed = true;
    }

    @Override
    public void fill(Color color) {
        if (pathPoints.size() < 3) {
            return; // Need at least 3 points for a polygon
        }

        // For fill, we need to work with unique vertices only
        // If closePath() was called, it added a duplicate of the first vertex at the end
        // We need to remove it for triangulation (fill implicitly closes the polygon)
        List<Vector2> fillPoints = pathPoints;
        if (pathClosed && pathPoints.size() > 3) {
            // Check if last point is duplicate of first (added by closePath)
            Vector2 first = pathPoints.get(0);
            Vector2 last = pathPoints.get(pathPoints.size() - 1);
            float dx = last.getX() - first.getX();
            float dy = last.getY() - first.getY();
            if (dx * dx + dy * dy < 0.0001f) {
                // Last point is a duplicate, use all but last for triangulation
                fillPoints = pathPoints.subList(0, pathPoints.size() - 1);
            }
        }

        // Fast path for triangles - no triangulation needed
        if (fillPoints.size() == 3) {
            drawTriangle(fillPoints.get(0).getX(), fillPoints.get(0).getY(),
                        fillPoints.get(1).getX(), fillPoints.get(1).getY(),
                        fillPoints.get(2).getX(), fillPoints.get(2).getY(), color);
            return;
        }

        // Use ear clipping triangulation (works for any simple polygon - convex or concave)
        // This properly handles stars, irregular shapes, etc.
        List<Integer> triangleIndices = MathHelper.triangulate(fillPoints);
        
        // Draw each triangle
        for (int i = 0; i < triangleIndices.size(); i += 3) {
            Vector2 v0 = fillPoints.get(triangleIndices.get(i));
            Vector2 v1 = fillPoints.get(triangleIndices.get(i + 1));
            Vector2 v2 = fillPoints.get(triangleIndices.get(i + 2));
            
            drawTriangle(v0.getX(), v0.getY(), 
                        v1.getX(), v1.getY(),
                        v2.getX(), v2.getY(), color);
        }
    }

    @Override
    public void stroke(Color color, float lineWidth) {
        if (pathPoints.size() < 2) {
            return; // Need at least 2 points for a stroke
        }

        // Draw lines connecting all points
        // If path was closed via closePath(), the closing segment is already in pathPoints
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            Vector2 p1 = pathPoints.get(i);
            Vector2 p2 = pathPoints.get(i + 1);
            strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY(), lineWidth, color);
        }
    }

    /**
     * Helper method to draw a filled triangle.
     * Uses the batch renderer's native triangle support.
     */
    private void drawTriangle(float x1, float y1, float x2, float y2, float x3, float y3, Color color) {
        batchRenderer.fillTriangle(x1, y1, x2, y2, x3, y3, color);
    }

    // ==================== Culling API ====================
    
    /**
     * Enable or disable viewport culling optimization.
     * When enabled, primitives outside the viewport are automatically skipped.
     * Default: enabled.
     * 
     * @param enabled Whether to enable culling
     */
    public void setCullingEnabled(boolean enabled) {
        batchRenderer.setCullingEnabled(enabled);
    }
    
    /**
     * Manually set custom culling bounds.
     * Useful for scrolling canvases or custom viewport management.
     * 
     * @param minX Minimum X coordinate (left edge)
     * @param minY Minimum Y coordinate (top edge)
     * @param maxX Maximum X coordinate (right edge)
     * @param maxY Maximum Y coordinate (bottom edge)
     */
    public void setCullingBounds(float minX, float minY, float maxX, float maxY) {
        batchRenderer.setCullingBounds(minX, minY, maxX, maxY);
    }
    
    /**
     * Set culling bounds using a rectangle.
     * 
     * @param bounds The culling rectangle
     */
    public void setCullingBounds(Rectangle bounds) {
        batchRenderer.setCullingBounds(bounds.getX(), bounds.getY(), 
                                      bounds.getX() + bounds.getWidth(), 
                                      bounds.getY() + bounds.getHeight());
    }

    @Override
    public void dispose() {
        batchRenderer.dispose();
    }
}
