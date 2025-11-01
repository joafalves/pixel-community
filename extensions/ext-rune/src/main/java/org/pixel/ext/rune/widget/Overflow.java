package org.pixel.ext.rune.widget;

/**
 * Overflow behavior for containers (CSS-like).
 * Controls what happens when content exceeds container bounds.
 */
public enum Overflow {
    /**
     * Content is visible outside bounds (no clipping, no scrollbars).
     * Default behavior.
     */
    VISIBLE,
    
    /**
     * Content is clipped to bounds (no scrollbars).
     */
    HIDDEN,
    
    /**
     * Horizontal scrollbar appears when content is wider than container.
     */
    SCROLL_HORIZONTAL,
    
    /**
     * Vertical scrollbar appears when content is taller than container.
     */
    SCROLL_VERTICAL,
    
    /**
     * Both scrollbars appear when needed (most common for scrollable panels).
     */
    SCROLL
}
