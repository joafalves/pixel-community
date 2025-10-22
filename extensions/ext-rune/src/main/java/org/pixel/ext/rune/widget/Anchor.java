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
     * Calculate the top-left corner X position based on anchor.
     * Zero-allocation alternative to calculatePosition().
     *
     * @param x The anchored x position
     * @param width Widget width
     * @return Actual X position (top-left corner)
     */
    public float calculateX(float x, float width) {
        switch (this) {
            case TOP_CENTER:
            case CENTER:
            case BOTTOM_CENTER:
                return x - width / 2f;
            case TOP_RIGHT:
            case CENTER_RIGHT:
            case BOTTOM_RIGHT:
                return x - width;
            default:  // TOP_LEFT, CENTER_LEFT, BOTTOM_LEFT
                return x;
        }
    }

    /**
     * Calculate the top-left corner Y position based on anchor.
     * Zero-allocation alternative to calculatePosition().
     *
     * @param y The anchored y position
     * @param height Widget height
     * @return Actual Y position (top-left corner)
     */
    public float calculateY(float y, float height) {
        switch (this) {
            case CENTER_LEFT:
            case CENTER:
            case CENTER_RIGHT:
                return y - height / 2f;
            case BOTTOM_LEFT:
            case BOTTOM_CENTER:
            case BOTTOM_RIGHT:
                return y - height;
            default:  // TOP_LEFT, TOP_CENTER, TOP_RIGHT
                return y;
        }
    }

    /**
     * Calculate the top-left corner position based on anchor.
     * DEPRECATED: Use calculateX() and calculateY() to avoid allocation.
     *
     * @param x The anchored x position
     * @param y The anchored y position
     * @param width Widget width
     * @param height Widget height
     * @return Array [actualX, actualY] representing top-left corner
     * @deprecated Use {@link #calculateX(float, float)} and {@link #calculateY(float, float)} instead
     */
    @Deprecated
    public float[] calculatePosition(float x, float y, float width, float height) {
        return new float[] { calculateX(x, width), calculateY(y, height) };
    }
}
