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
    
    // === Common Preset Constants (Zero-GC) ===
    
    /** Top-left alignment (default) - X is left edge, Y is top of text */
    public static final TextAlign TOP_LEFT = new TextAlign(Horizontal.LEFT, Vertical.TOP);
    
    /** Top-center alignment - X is horizontal center, Y is top of text */
    public static final TextAlign TOP_CENTER = new TextAlign(Horizontal.CENTER, Vertical.TOP);
    
    /** Top-right alignment - X is right edge, Y is top of text */
    public static final TextAlign TOP_RIGHT = new TextAlign(Horizontal.RIGHT, Vertical.TOP);
    
    /** Middle-left alignment - X is left edge, Y is vertical center */
    public static final TextAlign MIDDLE_LEFT = new TextAlign(Horizontal.LEFT, Vertical.MIDDLE);
    
    /** Middle-center alignment - X is horizontal center, Y is vertical center */
    public static final TextAlign MIDDLE_CENTER = new TextAlign(Horizontal.CENTER, Vertical.MIDDLE);
    
    /** Middle-right alignment - X is right edge, Y is vertical center */
    public static final TextAlign MIDDLE_RIGHT = new TextAlign(Horizontal.RIGHT, Vertical.MIDDLE);
    
    /** Bottom-left alignment - X is left edge, Y is bottom of text */
    public static final TextAlign BOTTOM_LEFT = new TextAlign(Horizontal.LEFT, Vertical.BOTTOM);
    
    /** Bottom-center alignment - X is horizontal center, Y is bottom of text */
    public static final TextAlign BOTTOM_CENTER = new TextAlign(Horizontal.CENTER, Vertical.BOTTOM);
    
    /** Bottom-right alignment - X is right edge, Y is bottom of text */
    public static final TextAlign BOTTOM_RIGHT = new TextAlign(Horizontal.RIGHT, Vertical.BOTTOM);
    
    /** Baseline-left alignment - X is left edge, Y is baseline */
    public static final TextAlign BASELINE_LEFT = new TextAlign(Horizontal.LEFT, Vertical.BASELINE);
    
    /** Baseline-center alignment - X is horizontal center, Y is baseline */
    public static final TextAlign BASELINE_CENTER = new TextAlign(Horizontal.CENTER, Vertical.BASELINE);
    
    /** Baseline-right alignment - X is right edge, Y is baseline */
    public static final TextAlign BASELINE_RIGHT = new TextAlign(Horizontal.RIGHT, Vertical.BASELINE);
    
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
}
