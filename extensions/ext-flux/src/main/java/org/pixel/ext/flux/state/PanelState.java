/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.flux.state;

import org.pixel.math.Vector2;

/**
 * State for panel widgets.
 * Stores scroll offset, content height, position, and collapsed state.
 */
public class PanelState extends WidgetState {
    
    private float scrollOffset = 0;
    private float contentHeight = 0;
    private final Vector2 position = new Vector2();
    private float width = 0;
    private float height = 0;
    private boolean positionInitialized = false;
    private boolean collapsed = false;
    
    /**
     * Create panel state.
     * 
     * @param id Panel ID
     */
    public PanelState(String id) {
        super(id);
    }
    
    /**
     * Get the scroll offset.
     * 
     * @return Scroll offset in pixels
     */
    public float getScrollOffset() {
        return scrollOffset;
    }
    
    /**
     * Set the scroll offset.
     * 
     * @param offset New scroll offset
     */
    public void setScrollOffset(float offset) {
        this.scrollOffset = Math.max(0, offset);
    }
    
    /**
     * Get the total content height.
     * 
     * @return Content height in pixels
     */
    public float getContentHeight() {
        return contentHeight;
    }
    
    /**
     * Set the content height.
     * 
     * @param height New content height
     */
    public void setContentHeight(float height) {
        this.contentHeight = Math.max(0, height);
    }
    
    /**
     * Get the panel position.
     * 
     * @return Position vector
     */
    public Vector2 getPosition() {
        return position;
    }
    
    /**
     * Set the panel position.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     */
    public void setPosition(float x, float y) {
        this.position.set(x, y);
        this.positionInitialized = true;
    }
    
    /**
     * Get panel width.
     * 
     * @return Panel width
     */
    public float getWidth() {
        return width;
    }
    
    /**
     * Set panel width.
     * 
     * @param width Panel width
     */
    public void setWidth(float width) {
        this.width = width;
    }
    
    /**
     * Get panel height.
     * 
     * @return Panel height
     */
    public float getHeight() {
        return height;
    }
    
    /**
     * Set panel height.
     * 
     * @param height Panel height
     */
    public void setHeight(float height) {
        this.height = height;
    }
    
    /**
     * Check if position has been initialized.
     * 
     * @return True if position was set
     */
    public boolean isPositionInitialized() {
        return positionInitialized;
    }
    
    /**
     * Check if panel is collapsed.
     * 
     * @return True if collapsed
     */
    public boolean isCollapsed() {
        return collapsed;
    }
    
    /**
     * Set collapsed state.
     * 
     * @param collapsed True to collapse
     */
    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }
}
