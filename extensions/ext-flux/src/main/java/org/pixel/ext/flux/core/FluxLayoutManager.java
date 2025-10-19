/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.flux.core;

import java.util.Stack;

/**
 * Manages layout cursor positioning and nested layout contexts for Flux GUI.
 * Handles cursor advancement, same-line positioning, spacing, and layout stack management.
 */
public class FluxLayoutManager {
    
    private float cursorX;
    private float cursorY;
    private float lineHeight; // Height of current line (max widget height)
    
    private float lastWidgetX;
    private float lastWidgetY;
    private float lastWidgetWidth;
    private float lastWidgetHeight;
    
    private final Stack<LayoutContext> layoutStack = new Stack<>();
    
    private float defaultSpacing = 4.0f;
    private float defaultPadding = 8.0f;
    
    // Menu-specific cursor state (for menu popup layout)
    private float menuPopupCursorX = 0;
    private boolean insideMenuPopup = false;
    
    /**
     * Layout context for nested containers (panels, etc.).
     */
    public static class LayoutContext {
        public float cursorX;
        public float cursorY;
        public float lineHeight;
        public float contentX; // Content area X offset
        public float contentY; // Content area Y offset
        public float contentWidth; // Available width for content
        public float contentHeight; // Available height for content
        public float panelX; // Panel X position (for scrollbar positioning)
        public float panelWidth; // Panel full width (for scrollbar positioning)
        public boolean hasScroll; // Whether scrolling is enabled
        public boolean hasScrollbar; // Whether to show scrollbar
        
        public LayoutContext(float cursorX, float cursorY, float lineHeight,
                           float contentX, float contentY, float contentWidth, float contentHeight,
                           float panelX, float panelWidth,
                           boolean hasScroll, boolean hasScrollbar) {
            this.cursorX = cursorX;
            this.cursorY = cursorY;
            this.lineHeight = lineHeight;
            this.contentX = contentX;
            this.contentY = contentY;
            this.contentWidth = contentWidth;
            this.contentHeight = contentHeight;
            this.panelX = panelX;
            this.panelWidth = panelWidth;
            this.hasScroll = hasScroll;
            this.hasScrollbar = hasScrollbar;
        }
    }
    
    /**
     * Get current cursor X position.
     * 
     * @return Cursor X coordinate
     */
    public float getCursorX() {
        return cursorX;
    }
    
    /**
     * Get current cursor Y position.
     * 
     * @return Cursor Y coordinate
     */
    public float getCursorY() {
        return cursorY;
    }
    
    /**
     * Get current line height.
     * 
     * @return Line height
     */
    public float getLineHeight() {
        return lineHeight;
    }
    
    /**
     * Set cursor position directly.
     * 
     * @param x New X coordinate
     * @param y New Y coordinate
     */
    public void setCursor(float x, float y) {
        this.cursorX = x;
        this.cursorY = y;
    }
    
    /**
     * Set cursor X position.
     * 
     * @param x New X coordinate
     */
    public void setCursorX(float x) {
        this.cursorX = x;
    }
    
    /**
     * Set cursor Y position.
     * 
     * @param y New Y coordinate
     */
    public void setCursorY(float y) {
        this.cursorY = y;
    }
    
    /**
     * Set line height.
     * 
     * @param height New line height
     */
    public void setLineHeight(float height) {
        this.lineHeight = height;
    }
    
    /**
     * Reset layout state to initial padding.
     * 
     * @param padding Initial padding value
     */
    public void reset(float padding) {
        cursorX = padding;
        cursorY = padding;
        lineHeight = 0;
        layoutStack.clear();
        insideMenuPopup = false;
        menuPopupCursorX = 0;
    }
    
    /**
     * Set default spacing between widgets.
     * 
     * @param spacing Spacing in pixels
     */
    public void setDefaultSpacing(float spacing) {
        this.defaultSpacing = spacing;
    }
    
    /**
     * Get default spacing.
     * 
     * @return Default spacing
     */
    public float getDefaultSpacing() {
        return defaultSpacing;
    }
    
    /**
     * Set default padding.
     * 
     * @param padding Padding in pixels
     */
    public void setDefaultPadding(float padding) {
        this.defaultPadding = padding;
    }
    
    /**
     * Get default padding.
     * 
     * @return Default padding
     */
    public float getDefaultPadding() {
        return defaultPadding;
    }
    
    /**
     * Advance cursor after placing a widget.
     * Moves cursor down to next line and resets X to left margin.
     * 
     * @param width Widget width
     * @param height Widget height
     */
    public void advanceCursor(float width, float height) {
        // Save widget position and dimensions BEFORE moving cursor
        lastWidgetX = cursorX;
        lastWidgetY = cursorY;
        lastWidgetWidth = width;
        lastWidgetHeight = height;
        
        // Move cursor down to next line
        if (lineHeight > 0) {
            cursorY += lineHeight + defaultSpacing;
        } else {
            cursorY += height + defaultSpacing;
        }
        
        // Reset to left margin (unless we're inside a menu popup)
        if (insideMenuPopup) {
            // Inside menu popup - preserve the popup cursor X
            cursorX = menuPopupCursorX;
        } else if (layoutStack.isEmpty()) {
            cursorX = defaultPadding;
        } else {
            LayoutContext ctx = layoutStack.peek();
            cursorX = ctx.contentX + defaultPadding;
        }
        
        lineHeight = 0;
    }
    
    /**
     * Keep next widget on same line as previous with default spacing.
     */
    public void sameLine() {
        cursorY = lastWidgetY;
        cursorX = lastWidgetX + lastWidgetWidth + defaultSpacing;
        lineHeight = Math.max(lineHeight, lastWidgetHeight);
    }
    
    /**
     * Keep next widget on same line with custom horizontal offset.
     * 
     * @param offsetX Horizontal offset from previous widget
     */
    public void sameLine(float offsetX) {
        cursorY = lastWidgetY;
        cursorX = lastWidgetX + lastWidgetWidth + offsetX;
        lineHeight = Math.max(lineHeight, lastWidgetHeight);
    }
    
    /**
     * Add vertical spacing.
     * 
     * @param height Spacing height in pixels
     */
    public void spacing(float height) {
        advanceCursor(0, height);
    }
    
    /**
     * Push a new layout context onto the stack (entering a nested container).
     * 
     * @param context Layout context to push
     */
    public void pushContext(LayoutContext context) {
        layoutStack.push(context);
    }
    
    /**
     * Pop the current layout context from the stack (exiting a nested container).
     * 
     * @return The popped context, or null if stack was empty
     */
    public LayoutContext popContext() {
        if (!layoutStack.isEmpty()) {
            return layoutStack.pop();
        }
        return null;
    }
    
    /**
     * Peek at the current layout context without removing it.
     * 
     * @return Current context, or null if stack is empty
     */
    public LayoutContext peekContext() {
        if (!layoutStack.isEmpty()) {
            return layoutStack.peek();
        }
        return null;
    }
    
    /**
     * Check if layout stack is empty.
     * 
     * @return True if no nested contexts
     */
    public boolean isLayoutStackEmpty() {
        return layoutStack.isEmpty();
    }
    
    /**
     * Get last widget X position.
     * 
     * @return Last widget X
     */
    public float getLastWidgetX() {
        return lastWidgetX;
    }
    
    /**
     * Get last widget Y position.
     * 
     * @return Last widget Y
     */
    public float getLastWidgetY() {
        return lastWidgetY;
    }
    
    /**
     * Get last widget width.
     * 
     * @return Last widget width
     */
    public float getLastWidgetWidth() {
        return lastWidgetWidth;
    }
    
    /**
     * Get last widget height.
     * 
     * @return Last widget height
     */
    public float getLastWidgetHeight() {
        return lastWidgetHeight;
    }
    
    /**
     * Set menu popup cursor X (for menu layout).
     * 
     * @param x Cursor X position
     */
    public void setMenuPopupCursorX(float x) {
        this.menuPopupCursorX = x;
    }
    
    /**
     * Get menu popup cursor X.
     * 
     * @return Menu popup cursor X
     */
    public float getMenuPopupCursorX() {
        return menuPopupCursorX;
    }
    
    /**
     * Set whether currently inside menu popup.
     * 
     * @param inside True if inside menu popup
     */
    public void setInsideMenuPopup(boolean inside) {
        this.insideMenuPopup = inside;
    }
    
    /**
     * Check if currently inside menu popup.
     * 
     * @return True if inside menu popup
     */
    public boolean isInsideMenuPopup() {
        return insideMenuPopup;
    }
}
