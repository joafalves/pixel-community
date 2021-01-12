/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

import org.pixel.commons.model.HorizontalAlignment;
import org.pixel.commons.model.Solidity;
import org.pixel.commons.model.VerticalAlignment;
import org.pixel.graphics.Color;
import org.pixel.math.FSize;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

public abstract class RenderEngine2D extends StatefulRenderEngine {

    /**
     * Constructor
     *
     * @param viewportDimensions
     */
    public RenderEngine2D(Rectangle viewportDimensions) {
        super(viewportDimensions);
    }

    /**
     * @param position
     */
    public void translate(Vector2 position) {
        translate(position.getX(), position.getY());
    }

    /**
     * @param x
     * @param y
     */
    public abstract void translate(float x, float y);

    /**
     *
     */
    public abstract void beginPath();

    /**
     * @param bounds
     * @param cornerRadius
     */
    public void roundedRectangle(Rectangle bounds, float cornerRadius) {
        roundedRectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), cornerRadius);
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @param cornerRadius
     */
    public abstract void roundedRectangle(float x, float y, float width, float height, float cornerRadius);

    /**
     * @param bounds
     */
    public void rectangle(Rectangle bounds) {
        rectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public abstract void rectangle(float x, float y, float width, float height);

    /**
     * @param startPosition
     * @param endPosition
     * @param startColor
     * @param endColor
     */
    public void linearGradient(Vector2 startPosition, Vector2 endPosition, Color startColor, Color endColor) {
        linearGradient(startPosition.getX(), startPosition.getY(), endPosition.getX(), endPosition.getY(), startColor, endColor);
    }

    /**
     * @param sx
     * @param sy
     * @param ex
     * @param ey
     * @param startColor
     * @param endColor
     */
    public abstract void linearGradient(float sx, float sy, float ex, float ey, Color startColor, Color endColor);

    /**
     * @param bounds
     * @param radius
     * @param feather
     * @param startColor
     * @param endColor
     */
    public void boxGradient(Rectangle bounds, float radius, float feather, Color startColor, Color endColor) {
        boxGradient(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), radius, feather, startColor, endColor);
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @param radius
     * @param feather
     * @param startColor
     * @param endColor
     */
    public abstract void boxGradient(float x, float y, float width, float height, float radius, float feather,
                                     Color startColor, Color endColor);

    /**
     * @param color
     */
    public abstract void fillColor(Color color);

    /**
     * @param width
     */
    public abstract void strokeWidth(float width);

    /**
     * @param color
     */
    public abstract void strokeColor(Color color);

    /**
     * @param position
     */
    public void moveTo(Vector2 position) {
        moveTo(position.getX(), position.getY());
    }

    /**
     * @param x
     * @param y
     */
    public abstract void moveTo(float x, float y);

    /**
     * @param position
     */
    public void lineTo(Vector2 position) {
        lineTo(position.getX(), position.getY());
    }

    /**
     * @param x
     * @param y
     */
    public abstract void lineTo(float x, float y);

    /**
     * @param controlPosition1
     * @param controlPosition2
     * @param position
     */
    public void bezierCurveTo(Vector2 controlPosition1, Vector2 controlPosition2, Vector2 position) {
        bezierCurveTo(controlPosition1.getX(), controlPosition1.getY(), controlPosition2.getX(), controlPosition2.getY(),
                position.getX(), position.getY());
    }

    /**
     * @param cp1x
     * @param cp1y
     * @param cp2x
     * @param cp2y
     * @param x
     * @param y
     */
    public abstract void bezierCurveTo(float cp1x, float cp1y, float cp2x, float cp2y, float x, float y);


    /**
     * @param controlPoint
     * @param position
     */
    public void quadraticCurveTo(Vector2 controlPoint, Vector2 position) {
        quadraticCurveTo(controlPoint.getX(), controlPoint.getY(), position.getX(), position.getY());
    }

    /**
     * @param cpx
     * @param cpy
     * @param x
     * @param y
     */
    public abstract void quadraticCurveTo(float cpx, float cpy, float x, float y);

    /**
     * @param position
     * @param radius
     */
    public void circle(Vector2 position, float radius) {
        circle(position.getX(), position.getY(), radius);
    }

    /**
     * @param x
     * @param y
     * @param radius
     */
    public abstract void circle(float x, float y, float radius);

    /**
     *
     */
    public abstract void closePath();

    /**
     *
     */
    public abstract void fillPaint();

    /**
     *
     */
    public abstract void stroke();

    /**
     *
     */
    public abstract void fill();

    /**
     * @param bounds
     */
    public void scissor(Rectangle bounds) {
        scissor(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public abstract void scissor(float x, float y, float width, float height);

    /**
     * @param fontName
     */
    public abstract void setFontName(String fontName);

    /**
     * @param fontSize
     */
    public abstract void setFontSize(Float fontSize);

    /**
     * @param horizontalAlignment
     * @param verticalAlignment
     */
    public abstract void setTextAlignment(HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment);

    /**
     * @param text
     * @param position
     * @param maxRowWidth
     */
    public void fillText(String text, Vector2 position, float maxRowWidth) {
        // draw request
        fillText(text, position.getX(), position.getY(), maxRowWidth);
    }

    /**
     * @param text
     * @param x
     * @param y
     * @param maxRowWidth
     */
    public abstract void fillText(String text, float x, float y, float maxRowWidth);

    /**
     * @param text
     * @param position
     * @param maxRowWidth
     */
    public abstract void strokeText(String text, Vector2 position, float maxRowWidth);

    /**
     * @param text
     * @param position
     * @param maxRowWidth
     * @return
     */
    public abstract FSize measureText(String text, Vector2 position, float maxRowWidth);

    /**
     * @param solidity
     */
    public abstract void setSolidity(Solidity solidity);
}
