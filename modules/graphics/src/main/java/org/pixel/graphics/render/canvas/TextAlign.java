/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import lombok.Getter;
import lombok.Setter;

/**
 * Text alignment options for horizontal and vertical positioning.
 */
@Setter
@Getter
public class TextAlign {
    
    /**
     * Horizontal text alignment.
     */
    public enum Horizontal {
        /** Align text to the left (X is left edge) - DEFAULT */
        LEFT,
        
        /** Center text horizontally (X is center point) */
        CENTER,
        
        /** Align text to the right (X is right edge) */
        RIGHT
    }
    
    /**
     * Vertical text alignment.
     */
    public enum Vertical {
        /** Align to top (Y is top of tallest glyph) - DEFAULT */
        TOP,
        
        /** Align to middle (Y is vertical center of text) */
        MIDDLE,
        
        /** Align to baseline (Y is baseline of text) */
        BASELINE,
        
        /** Align to bottom (Y is bottom of lowest descender) */
        BOTTOM
    }
    
    private Horizontal horizontal;
    private Vertical vertical;
    
    /**
     * Create default alignment (LEFT, TOP).
     */
    public TextAlign() {
        this.horizontal = Horizontal.LEFT;
        this.vertical = Vertical.TOP;
    }
    
    /**
     * Create text alignment with specified horizontal and vertical alignment.
     */
    public TextAlign(Horizontal horizontal, Vertical vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }

    // === Common Presets ===
    
    public static TextAlign topLeft() {
        return new TextAlign(Horizontal.LEFT, Vertical.TOP);
    }
    
    public static TextAlign topCenter() {
        return new TextAlign(Horizontal.CENTER, Vertical.TOP);
    }
    
    public static TextAlign topRight() {
        return new TextAlign(Horizontal.RIGHT, Vertical.TOP);
    }
    
    public static TextAlign middleLeft() {
        return new TextAlign(Horizontal.LEFT, Vertical.MIDDLE);
    }
    
    public static TextAlign middleCenter() {
        return new TextAlign(Horizontal.CENTER, Vertical.MIDDLE);
    }
    
    public static TextAlign middleRight() {
        return new TextAlign(Horizontal.RIGHT, Vertical.MIDDLE);
    }
    
    public static TextAlign bottomLeft() {
        return new TextAlign(Horizontal.LEFT, Vertical.BOTTOM);
    }
    
    public static TextAlign bottomCenter() {
        return new TextAlign(Horizontal.CENTER, Vertical.BOTTOM);
    }
    
    public static TextAlign bottomRight() {
        return new TextAlign(Horizontal.RIGHT, Vertical.BOTTOM);
    }
    
    public static TextAlign baselineLeft() {
        return new TextAlign(Horizontal.LEFT, Vertical.BASELINE);
    }
    
    public static TextAlign baselineCenter() {
        return new TextAlign(Horizontal.CENTER, Vertical.BASELINE);
    }
    
    public static TextAlign baselineRight() {
        return new TextAlign(Horizontal.RIGHT, Vertical.BASELINE);
    }
}
