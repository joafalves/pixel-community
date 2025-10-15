/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import org.pixel.commons.Color;
import org.pixel.math.Vector2;

/**
 * Text rendering style configuration.
 * Supports fill color, stroke (outline), drop shadow, and spacing.
 */
public class TextStyle {
    
    private Color fillColor;
    private Color strokeColor;
    private float strokeWidth;
    private boolean dropShadow;
    private Vector2 shadowOffset;
    private Color shadowColor;
    private float letterSpacing;

    /**
     * Create default text style (white fill, no stroke/shadow).
     */
    public TextStyle() {
        this.fillColor = Color.WHITE;
        this.strokeColor = null;
        this.strokeWidth = 0f;
        this.dropShadow = false;
        this.shadowOffset = new Vector2(2, 2);
        this.shadowColor = new Color(0, 0, 0, 0.5f);
        this.letterSpacing = 1f;
    }

    /**
     * Create text style with fill color.
     */
    public TextStyle(Color fillColor) {
        this();
        this.fillColor = fillColor;
    }

    // === Fluent Setters ===

    public TextStyle setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    public TextStyle setStroke(Color strokeColor, float strokeWidth) {
        this.strokeColor = strokeColor;
        this.strokeWidth = strokeWidth;
        return this;
    }

    /**
     * Fluent alias for setStroke.
     */
    public TextStyle withStroke(Color strokeColor, float strokeWidth) {
        return setStroke(strokeColor, strokeWidth);
    }

    public TextStyle setDropShadow(boolean enabled) {
        this.dropShadow = enabled;
        return this;
    }

    public TextStyle setDropShadow(Vector2 offset, Color color) {
        this.dropShadow = true;
        this.shadowOffset = offset;
        this.shadowColor = color;
        return this;
    }

    /**
     * Fluent method to set drop shadow with offset coordinates.
     */
    public TextStyle withShadow(float offsetX, float offsetY, Color color) {
        this.dropShadow = true;
        this.shadowOffset = new Vector2(offsetX, offsetY);
        this.shadowColor = color;
        return this;
    }

    public TextStyle setLetterSpacing(float spacing) {
        this.letterSpacing = spacing;
        return this;
    }

    /**
     * Fluent alias for setLetterSpacing.
     */
    public TextStyle withLetterSpacing(float spacing) {
        this.letterSpacing = spacing;
        return this;
    }

    // === Getters ===

    public Color getFillColor() {
        return fillColor;
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public boolean hasStroke() {
        return strokeColor != null && strokeWidth > 0;
    }

    public boolean isDropShadow() {
        return dropShadow;
    }

    public Vector2 getShadowOffset() {
        return shadowOffset;
    }

    public Color getShadowColor() {
        return shadowColor;
    }

    public float getLetterSpacing() {
        return letterSpacing;
    }

    // === Common Presets ===

    public static TextStyle outlined(Color fill, Color stroke, float strokeWidth) {
        return new TextStyle(fill).setStroke(stroke, strokeWidth);
    }

    public static TextStyle withShadow(Color fill) {
        return new TextStyle(fill).setDropShadow(true);
    }
}
