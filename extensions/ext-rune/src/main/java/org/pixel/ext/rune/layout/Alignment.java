/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.rune.layout;

import lombok.Getter;

/**
 * Defines alignment within a container.
 * Similar to CSS flexbox alignment, with horizontal and vertical components.
 * 
 * <p>Common presets:
 * <pre>
 * Alignment.topLeft()
 * Alignment.center()
 * Alignment.bottomRight()
 * </pre>
 */
@Getter
public class Alignment {
    
    /**
     * Horizontal alignment options.
     */
    public enum Horizontal {
        LEFT,    // Align to left edge
        CENTER,  // Center horizontally
        RIGHT,   // Align to right edge
        STRETCH  // Stretch to fill width
    }
    
    /**
     * Vertical alignment options.
     */
    public enum Vertical {
        TOP,     // Align to top edge
        CENTER,  // Center vertically
        BOTTOM,  // Align to bottom edge
        STRETCH  // Stretch to fill height
    }
    
    private final Horizontal horizontal;
    private final Vertical vertical;
    
    /**
     * Create alignment with horizontal and vertical components.
     */
    public Alignment(Horizontal horizontal, Vertical vertical) {
        this.horizontal = horizontal != null ? horizontal : Horizontal.LEFT;
        this.vertical = vertical != null ? vertical : Vertical.TOP;
    }
    
    // === Common Presets ===
    
    public static Alignment topLeft() {
        return new Alignment(Horizontal.LEFT, Vertical.TOP);
    }
    
    public static Alignment topCenter() {
        return new Alignment(Horizontal.CENTER, Vertical.TOP);
    }
    
    public static Alignment topRight() {
        return new Alignment(Horizontal.RIGHT, Vertical.TOP);
    }
    
    public static Alignment centerLeft() {
        return new Alignment(Horizontal.LEFT, Vertical.CENTER);
    }
    
    public static Alignment center() {
        return new Alignment(Horizontal.CENTER, Vertical.CENTER);
    }
    
    public static Alignment centerRight() {
        return new Alignment(Horizontal.RIGHT, Vertical.CENTER);
    }
    
    public static Alignment bottomLeft() {
        return new Alignment(Horizontal.LEFT, Vertical.BOTTOM);
    }
    
    public static Alignment bottomCenter() {
        return new Alignment(Horizontal.CENTER, Vertical.BOTTOM);
    }
    
    public static Alignment bottomRight() {
        return new Alignment(Horizontal.RIGHT, Vertical.BOTTOM);
    }
    
    public static Alignment stretch() {
        return new Alignment(Horizontal.STRETCH, Vertical.STRETCH);
    }
    
    public static Alignment stretchTop() {
        return new Alignment(Horizontal.STRETCH, Vertical.TOP);
    }
    
    public static Alignment stretchCenter() {
        return new Alignment(Horizontal.STRETCH, Vertical.CENTER);
    }
    
    public static Alignment stretchBottom() {
        return new Alignment(Horizontal.STRETCH, Vertical.BOTTOM);
    }
    
    public static Alignment topStretch() {
        return new Alignment(Horizontal.LEFT, Vertical.STRETCH);
    }
    
    public static Alignment centerStretch() {
        return new Alignment(Horizontal.CENTER, Vertical.STRETCH);
    }
    
    public static Alignment rightStretch() {
        return new Alignment(Horizontal.RIGHT, Vertical.STRETCH);
    }
    
    /**
     * Calculate the x position for a child within a container.
     * 
     * @param containerX Container's x position
     * @param containerWidth Container's width
     * @param childWidth Child's width
     * @return X position for the child
     */
    public float calculateX(float containerX, float containerWidth, float childWidth) {
        return switch (horizontal) {
            case LEFT -> containerX;
            case CENTER -> containerX + (containerWidth - childWidth) / 2f;
            case RIGHT -> containerX + containerWidth - childWidth;
            case STRETCH -> containerX; // Position at left, width will be stretched separately
        };
    }
    
    /**
     * Calculate the y position for a child within a container.
     * 
     * @param containerY Container's y position
     * @param containerHeight Container's height
     * @param childHeight Child's height
     * @return Y position for the child
     */
    public float calculateY(float containerY, float containerHeight, float childHeight) {
        return switch (vertical) {
            case TOP -> containerY;
            case CENTER -> containerY + (containerHeight - childHeight) / 2f;
            case BOTTOM -> containerY + containerHeight - childHeight;
            case STRETCH -> containerY; // Position at top, height will be stretched separately
        };
    }
    
    /**
     * Check if horizontal alignment is STRETCH.
     */
    public boolean stretchesHorizontally() {
        return horizontal == Horizontal.STRETCH;
    }
    
    /**
     * Check if vertical alignment is STRETCH.
     */
    public boolean stretchesVertically() {
        return vertical == Vertical.STRETCH;
    }
    
    @Override
    public String toString() {
        return String.format("Alignment(%s, %s)", horizontal, vertical);
    }
}
