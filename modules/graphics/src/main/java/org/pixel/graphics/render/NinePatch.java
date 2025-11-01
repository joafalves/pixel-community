/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

import org.pixel.content.Texture;
import org.pixel.graphics.render.canvas.CanvasRenderer;
import org.pixel.math.Rectangle;

/**
 * Represents a 9-patch image that can be scaled intelligently while preserving corner and edge details.
 * 
 * <p>A 9-patch divides a texture into 9 regions:
 * <pre>
 * ┌─────┬─────────┬─────┐
 * │  1  │    2    │  3  │  Top row (corners + edge)
 * ├─────┼─────────┼─────┤
 * │  4  │    5    │  6  │  Middle row (edges + center)
 * ├─────┼─────────┼─────┤
 * │  7  │    8    │  9  │  Bottom row (corners + edge)
 * └─────┴─────────┴─────┘
 * </pre>
 * Corners (1,3,7,9) maintain fixed size, edges (2,4,6,8) stretch in one direction,
 * and the center (5) stretches in both directions.
 */
public class NinePatch {
    
    private final Texture texture;
    private final int left;   // Left border width in pixels
    private final int right;  // Right border width in pixels
    private final int top;    // Top border height in pixels
    private final int bottom; // Bottom border height in pixels
    
    /**
     * Create a 9-patch from a texture with symmetric borders.
     *
     * @param texture The source texture
     * @param border  Border size for all sides
     */
    public NinePatch(Texture texture, int border) {
        this(texture, border, border, border, border);
    }
    
    /**
     * Create a 9-patch from a texture with specified border sizes.
     *
     * @param texture The source texture
     * @param left    Left border width in pixels
     * @param right   Right border width in pixels
     * @param top     Top border height in pixels
     * @param bottom  Bottom border height in pixels
     */
    public NinePatch(Texture texture, int left, int right, int top, int bottom) {
        this.texture = texture;
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }
    
    /**
     * Render this 9-patch at the given position and size.
     * Submits 9 image draws to the canvas renderer, which will be batched efficiently.
     *
     * @param canvas The canvas renderer
     * @param x      Destination X position
     * @param y      Destination Y position
     * @param width  Destination width
     * @param height Destination height
     */
    public void render(CanvasRenderer canvas, float x, float y, float width, float height) {
        float texWidth = texture.getWidth();
        float texHeight = texture.getHeight();
        
        // Calculate the middle region dimensions
        float middleWidth = width - left - right;
        float middleHeight = height - top - bottom;
        
        // Texture coordinates for the 9 regions
        float texLeft = left;
        float texRight = texWidth - right;
        float texTop = top;
        float texBottom = texHeight - bottom;
        
        // Destination coordinates
        float destRight = x + width - right;
        float destBottom = y + height - bottom;
        float destMiddleX = x + left;
        float destMiddleY = y + top;
        
        // Create rectangles for source and destination regions
        // Reuse Rectangle objects to avoid allocations
        Rectangle src = new Rectangle();
        Rectangle dst = new Rectangle();
        
        // === TOP ROW ===
        
        // 1. Top-left corner (fixed size)
        src.set(0, 0, left, top);
        dst.set(x, y, left, top);
        canvas.drawImage(texture, src, dst);
        
        // 2. Top edge (stretched horizontally)
        src.set(texLeft, 0, texRight - texLeft, top);
        dst.set(destMiddleX, y, middleWidth, top);
        canvas.drawImage(texture, src, dst);
        
        // 3. Top-right corner (fixed size)
        src.set(texRight, 0, right, top);
        dst.set(destRight, y, right, top);
        canvas.drawImage(texture, src, dst);
        
        // === MIDDLE ROW ===
        
        // 4. Left edge (stretched vertically)
        src.set(0, texTop, left, texBottom - texTop);
        dst.set(x, destMiddleY, left, middleHeight);
        canvas.drawImage(texture, src, dst);
        
        // 5. Center (stretched both ways)
        src.set(texLeft, texTop, texRight - texLeft, texBottom - texTop);
        dst.set(destMiddleX, destMiddleY, middleWidth, middleHeight);
        canvas.drawImage(texture, src, dst);
        
        // 6. Right edge (stretched vertically)
        src.set(texRight, texTop, right, texBottom - texTop);
        dst.set(destRight, destMiddleY, right, middleHeight);
        canvas.drawImage(texture, src, dst);
        
        // === BOTTOM ROW ===
        
        // 7. Bottom-left corner (fixed size)
        src.set(0, texBottom, left, bottom);
        dst.set(x, destBottom, left, bottom);
        canvas.drawImage(texture, src, dst);
        
        // 8. Bottom edge (stretched horizontally)
        src.set(texLeft, texBottom, texRight - texLeft, bottom);
        dst.set(destMiddleX, destBottom, middleWidth, bottom);
        canvas.drawImage(texture, src, dst);
        
        // 9. Bottom-right corner (fixed size)
        src.set(texRight, texBottom, right, bottom);
        dst.set(destRight, destBottom, right, bottom);
        canvas.drawImage(texture, src, dst);
    }
    
    /**
     * Get the source texture.
     *
     * @return The texture
     */
    public Texture getTexture() {
        return texture;
    }
    
    /**
     * Get the left border width.
     *
     * @return Left border in pixels
     */
    public int getLeft() {
        return left;
    }
    
    /**
     * Get the right border width.
     *
     * @return Right border in pixels
     */
    public int getRight() {
        return right;
    }
    
    /**
     * Get the top border height.
     *
     * @return Top border in pixels
     */
    public int getTop() {
        return top;
    }
    
    /**
     * Get the bottom border height.
     *
     * @return Bottom border in pixels
     */
    public int getBottom() {
        return bottom;
    }
    
    /**
     * Get the minimum width required to render this 9-patch without distortion.
     *
     * @return Minimum width in pixels
     */
    public float getMinWidth() {
        return left + right;
    }
    
    /**
     * Get the minimum height required to render this 9-patch without distortion.
     *
     * @return Minimum height in pixels
     */
    public float getMinHeight() {
        return top + bottom;
    }
}
