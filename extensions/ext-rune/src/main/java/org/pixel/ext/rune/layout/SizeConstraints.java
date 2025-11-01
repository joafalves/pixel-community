/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.rune.layout;

import lombok.Getter;
import org.pixel.math.Size;

/**
 * Defines size constraints for widget layout.
 * Inspired by Flutter's BoxConstraints - provides min/max bounds for layout calculation.
 * 
 * <p>Example usage:
 * <pre>
 * // Fixed size
 * SizeConstraints.fixed(100, 50);
 * 
 * // Flexible with limits
 * SizeConstraints.builder()
 *     .minWidth(50)
 *     .maxWidth(200)
 *     .minHeight(30)
 *     .build();
 * 
 * // Fill available space
 * SizeConstraints.loose(500, 400); // max 500x400
 * </pre>
 */
@Getter
public class SizeConstraints {
    
    private final float minWidth;
    private final float minHeight;
    private final float maxWidth;
    private final float maxHeight;
    
    /**
     * Create size constraints with explicit bounds.
     */
    public SizeConstraints(float minWidth, float minHeight, float maxWidth, float maxHeight) {
        this.minWidth = Math.max(0, minWidth);
        this.minHeight = Math.max(0, minHeight);
        this.maxWidth = Math.max(this.minWidth, maxWidth);
        this.maxHeight = Math.max(this.minHeight, maxHeight);
    }
    
    /**
     * Create unconstrained size (0 to infinity).
     */
    public static SizeConstraints unconstrained() {
        return new SizeConstraints(0, 0, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    }
    
    /**
     * Create tight constraints (fixed size).
     */
    public static SizeConstraints fixed(float width, float height) {
        return new SizeConstraints(width, height, width, height);
    }
    
    /**
     * Create loose constraints (0 to max).
     */
    public static SizeConstraints loose(float maxWidth, float maxHeight) {
        return new SizeConstraints(0, 0, maxWidth, maxHeight);
    }
    
    /**
     * Create constraints that expand to fill available space.
     */
    public static SizeConstraints expand(float availableWidth, float availableHeight) {
        return new SizeConstraints(availableWidth, availableHeight, availableWidth, availableHeight);
    }
    
    /**
     * Constrain the given size to fit within these constraints.
     */
    public Size constrain(Size size) {
        return constrain(size.getWidth(), size.getHeight());
    }
    
    /**
     * Constrain the given width/height to fit within these constraints.
     */
    public Size constrain(float width, float height) {
        float w = Math.max(minWidth, Math.min(maxWidth, width));
        float h = Math.max(minHeight, Math.min(maxHeight, height));
        return new Size(w, h);
    }
    
    /**
     * Check if these constraints are tight (fixed size).
     */
    public boolean isTight() {
        return minWidth >= maxWidth && minHeight >= maxHeight;
    }
    
    /**
     * Check if width is bounded.
     */
    public boolean hasBoundedWidth() {
        return maxWidth < Float.POSITIVE_INFINITY;
    }
    
    /**
     * Check if height is bounded.
     */
    public boolean hasBoundedHeight() {
        return maxHeight < Float.POSITIVE_INFINITY;
    }
    
    /**
     * Create new constraints with width tightened to a specific value.
     */
    public SizeConstraints tightenWidth(float width) {
        return new SizeConstraints(width, minHeight, width, maxHeight);
    }
    
    /**
     * Create new constraints with height tightened to a specific value.
     */
    public SizeConstraints tightenHeight(float height) {
        return new SizeConstraints(minWidth, height, maxWidth, height);
    }
    
    /**
     * Create new constraints by shrinking the max bounds.
     */
    public SizeConstraints deflate(float horizontal, float vertical) {
        return new SizeConstraints(
            minWidth,
            minHeight,
            Math.max(minWidth, maxWidth - horizontal),
            Math.max(minHeight, maxHeight - vertical)
        );
    }
    
    /**
     * Create builder for fluent construction.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public String toString() {
        if (isTight()) {
            return String.format("SizeConstraints.fixed(%.1f, %.1f)", minWidth, minHeight);
        }
        return String.format("SizeConstraints(w: %.1f-%.1f, h: %.1f-%.1f)", 
            minWidth, maxWidth, minHeight, maxHeight);
    }
    
    /**
     * Builder for SizeConstraints.
     */
    public static class Builder {
        private float minWidth = 0;
        private float minHeight = 0;
        private float maxWidth = Float.POSITIVE_INFINITY;
        private float maxHeight = Float.POSITIVE_INFINITY;
        
        public Builder minWidth(float minWidth) {
            this.minWidth = minWidth;
            return this;
        }
        
        public Builder minHeight(float minHeight) {
            this.minHeight = minHeight;
            return this;
        }
        
        public Builder maxWidth(float maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }
        
        public Builder maxHeight(float maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }
        
        public Builder minSize(float width, float height) {
            this.minWidth = width;
            this.minHeight = height;
            return this;
        }
        
        public Builder maxSize(float width, float height) {
            this.maxWidth = width;
            this.maxHeight = height;
            return this;
        }
        
        public SizeConstraints build() {
            return new SizeConstraints(minWidth, minHeight, maxWidth, maxHeight);
        }
    }
}
