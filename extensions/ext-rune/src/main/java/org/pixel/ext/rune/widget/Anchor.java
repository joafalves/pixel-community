/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.ext.rune.widget;

/**
 * Anchor point for widget positioning.
 * 
 * <p>Defines what the widget's (x, y) position represents.
 * For example, if anchor is CENTER, then (x, y) represents the center point of the widget.
 * 
 * <p>Similar to Unity's RectTransform pivot or CSS transform-origin.
 */
public enum Anchor {
    /**
     * (x, y) represents the top-left corner.
     * This is the default and most common anchor point.
     */
    TOP_LEFT,
    
    /**
     * (x, y) represents the top-center point.
     */
    TOP_CENTER,
    
    /**
     * (x, y) represents the top-right corner.
     */
    TOP_RIGHT,
    
    /**
     * (x, y) represents the center-left point.
     */
    CENTER_LEFT,
    
    /**
     * (x, y) represents the center point.
     * Useful for centering dialogs or positioning relative to center.
     */
    CENTER,
    
    /**
     * (x, y) represents the center-right point.
     */
    CENTER_RIGHT,
    
    /**
     * (x, y) represents the bottom-left corner.
     */
    BOTTOM_LEFT,
    
    /**
     * (x, y) represents the bottom-center point.
     */
    BOTTOM_CENTER,
    
    /**
     * (x, y) represents the bottom-right corner.
     */
    BOTTOM_RIGHT;
    
    /**
     * Calculate the top-left corner position based on anchor.
     * 
     * @param x The anchored x position
     * @param y The anchored y position
     * @param width Widget width
     * @param height Widget height
     * @return Array [actualX, actualY] representing top-left corner
     */
    public float[] calculatePosition(float x, float y, float width, float height) {
        float actualX = x;
        float actualY = y;
        
        switch (this) {
            case TOP_LEFT:
                // Already top-left
                break;
            case TOP_CENTER:
                actualX = x - width / 2f;
                break;
            case TOP_RIGHT:
                actualX = x - width;
                break;
            case CENTER_LEFT:
                actualY = y - height / 2f;
                break;
            case CENTER:
                actualX = x - width / 2f;
                actualY = y - height / 2f;
                break;
            case CENTER_RIGHT:
                actualX = x - width;
                actualY = y - height / 2f;
                break;
            case BOTTOM_LEFT:
                actualY = y - height;
                break;
            case BOTTOM_CENTER:
                actualX = x - width / 2f;
                actualY = y - height;
                break;
            case BOTTOM_RIGHT:
                actualX = x - width;
                actualY = y - height;
                break;
        }
        
        return new float[] { actualX, actualY };
    }
}
