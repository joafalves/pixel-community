/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

import org.pixel.commons.Color;
import org.pixel.commons.attribute.HorizontalAlignment;
import org.pixel.commons.attribute.Solidity;
import org.pixel.commons.attribute.VerticalAlignment;
import org.pixel.math.Size;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

public abstract class RenderEngine2D extends StatefulRenderEngine {

    /**
     * Constructor.
     *
     * @param viewportDimensions The dimensions of the viewport.
     */
    public RenderEngine2D(Rectangle viewportDimensions) {
        super(viewportDimensions);
    }

    /**
     * Transforms the current coordinate system by the given matrix.
     *
     * @param viewMatrix The matrix to transform the coordinate system by.
     */
    public abstract void transform(Matrix4 viewMatrix);

    /**
     * Translate the current coordinate system by the given amount.
     *
     * @param position The position to translate to.
     */
    public void translate(Vector2 position) {
        translate(position.getX(), position.getY());
    }

    /**
     * Translates the current coordinate system by the given amount.
     *
     * @param x The x-coordinate to translate to.
     * @param y The y-coordinate to translate to.
     */
    public abstract void translate(float x, float y);

    /**
     * Rotates the current coordinate system by the given angle.
     *
     * @param angle The angle to rotate by.
     */
    public abstract void rotate(float angle);

    /**
     * Scale the current coordinate system by the given amount.
     *
     * @param x The x-scale to scale by.
     * @param y The y-scale to scale by.
     */
    public abstract void scale(float x, float y);

    /**
     * Begin a new path.
     */
    public abstract void beginPath();

    /**
     * Defines a rounded rectangle shape.
     *
     * @param bounds       The bounds of the rounded rectangle.
     * @param cornerRadius The radius of the rounded corners.
     */
    public void roundedRectangle(Rectangle bounds, float cornerRadius) {
        roundedRectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), cornerRadius);
    }

    /**
     * Defines a rounded rectangle shape.
     *
     * @param x            The x-coordinate of the rectangle.
     * @param y            The y-coordinate of the rectangle.
     * @param width        The width of the rectangle.
     * @param height       The height of the rectangle.
     * @param cornerRadius The radius of the rounded corners.
     */
    public abstract void roundedRectangle(float x, float y, float width, float height, float cornerRadius);

    /**
     * Defines a rectangle shape.
     *
     * @param bounds The bounds of the rectangle.
     */
    public void rectangle(Rectangle bounds) {
        rectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    /**
     * Defines a rectangle shape.
     *
     * @param x      The x-coordinate of the rectangle.
     * @param y      The y-coordinate of the rectangle.
     * @param width  The width of the rectangle.
     * @param height The height of the rectangle.
     */
    public abstract void rectangle(float x, float y, float width, float height);

    /**
     * Defines a linear gradient color.
     *
     * @param startPosition The start position of the gradient.
     * @param endPosition   The end position of the gradient.
     * @param startColor    The start color of the gradient.
     * @param endColor      The end color of the gradient.
     */
    public void linearGradient(Vector2 startPosition, Vector2 endPosition, Color startColor, Color endColor) {
        linearGradient(startPosition.getX(), startPosition.getY(), endPosition.getX(), endPosition.getY(), startColor,
                endColor);
    }

    /**
     * Defines a linear gradient color.
     *
     * @param sx         The x-coordinate of the start position.
     * @param sy         The y-coordinate of the start position.
     * @param ex         The x-coordinate of the end position.
     * @param ey         The y-coordinate of the end position.
     * @param startColor The start color of the gradient.
     * @param endColor   The end color of the gradient.
     */
    public abstract void linearGradient(float sx, float sy, float ex, float ey, Color startColor, Color endColor);

    /**
     * Defines a box gradient color.
     *
     * @param bounds     The bounds of the gradient.
     * @param radius     The radius of the gradient.
     * @param feather    The feather of the gradient.
     * @param startColor The start color of the gradient.
     * @param endColor   The end color of the gradient.
     */
    public void boxGradient(Rectangle bounds, float radius, float feather, Color startColor, Color endColor) {
        boxGradient(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), radius, feather, startColor,
                endColor);
    }

    /**
     * Defines a box gradient color.
     *
     * @param x          The x-coordinate of the gradient.
     * @param y          The y-coordinate of the gradient.
     * @param width      The width of the gradient.
     * @param height     The height of the gradient.
     * @param radius     The radius of the gradient.
     * @param feather    The feather of the gradient.
     * @param startColor The start color of the gradient.
     * @param endColor   The end color of the gradient.
     */
    public abstract void boxGradient(float x, float y, float width, float height, float radius, float feather,
            Color startColor, Color endColor);

    /**
     * Defines the fill color.
     *
     * @param color The fill color.
     */
    public abstract void fillColor(Color color);

    /**
     * Defines the stroke width.
     *
     * @param width The stroke width.
     */
    public abstract void strokeWidth(float width);

    /**
     * Defines the stroke color.
     *
     * @param color The stroke color.
     */
    public abstract void strokeColor(Color color);

    /**
     * Moves the current drawing position to the specified position.
     *
     * @param position The position to move to.
     */
    public void moveTo(Vector2 position) {
        moveTo(position.getX(), position.getY());
    }

    /**
     * Moves the current drawing position to the specified position.
     *
     * @param x The x-coordinate of the position to move to.
     * @param y The y-coordinate of the position to move to.
     */
    public abstract void moveTo(float x, float y);

    /**
     * Defines a line from the current drawing position to the specified position.
     *
     * @param position The position to draw to.
     */
    public void lineTo(Vector2 position) {
        lineTo(position.getX(), position.getY());
    }

    /**
     * Defines a line from the current drawing position to the specified position.
     *
     * @param x The x-coordinate of the position to draw to.
     * @param y The y-coordinate of the position to draw to.
     */
    public abstract void lineTo(float x, float y);

    /**
     * Defines a bezier curve from the current drawing position to the specified position.
     *
     * @param controlPosition1 The first control position.
     * @param controlPosition2 The second control position.
     * @param position         The position to draw to.
     */
    public void bezierCurveTo(Vector2 controlPosition1, Vector2 controlPosition2, Vector2 position) {
        bezierCurveTo(controlPosition1.getX(), controlPosition1.getY(), controlPosition2.getX(),
                controlPosition2.getY(), position.getX(), position.getY());
    }

    /**
     * Defines a bezier curve from the current drawing position to the specified position.
     *
     * @param cp1x The x-coordinate of the first control position.
     * @param cp1y The y-coordinate of the first control position.
     * @param cp2x The x-coordinate of the second control position.
     * @param cp2y The y-coordinate of the second control position.
     * @param x    The x-coordinate of the position to draw to.
     * @param y    The y-coordinate of the position to draw to.
     */
    public abstract void bezierCurveTo(float cp1x, float cp1y, float cp2x, float cp2y, float x, float y);

    /**
     * Defines a quadratic curve from the current drawing position to the specified position.
     *
     * @param controlPoint The control point.
     * @param position     The position to draw to.
     */
    public void quadraticCurveTo(Vector2 controlPoint, Vector2 position) {
        quadraticCurveTo(controlPoint.getX(), controlPoint.getY(), position.getX(), position.getY());
    }

    /**
     * Defines a quadratic curve from the current drawing position to the specified position.
     *
     * @param cpx The x-coordinate of the control point.
     * @param cpy The y-coordinate of the control point.
     * @param x   The x-coordinate of the position to draw to.
     * @param y   The y-coordinate of the position to draw to.
     */
    public abstract void quadraticCurveTo(float cpx, float cpy, float x, float y);

    /**
     * Defines a circle shape.
     *
     * @param position The position of the center of the circle.
     * @param radius   The radius of the circle.
     */
    public void circle(Vector2 position, float radius) {
        circle(position.getX(), position.getY(), radius);
    }

    /**
     * Defines a circle shape.
     *
     * @param x      The x-coordinate of the center of the circle.
     * @param y      The y-coordinate of the center of the circle.
     * @param radius The radius of the circle.
     */
    public abstract void circle(float x, float y, float radius);

    /**
     * Ends the current path.
     */
    public abstract void endPath();

    /**
     * Fills the current path.
     */
    public abstract void fillPaint();

    /**
     * Strokes the current path.
     */
    public abstract void stroke();

    /**
     * Fills the current path.
     */
    public abstract void fill();

    /**
     * Scissors view by the given bounds.
     *
     * @param bounds The bounds to scissors by.
     */
    public void scissor(Rectangle bounds) {
        scissor(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    /**
     * Scissors view by the given bounds.
     *
     * @param x      The x-coordinate of the top-left corner of the bounds.
     * @param y      The y-coordinate of the top-left corner of the bounds.
     * @param width  The width of the bounds.
     * @param height The height of the bounds.
     */
    public abstract void scissor(float x, float y, float width, float height);

    /**
     * Set the font name.
     *
     * @param fontName The font name.
     */
    public abstract void setFontName(String fontName);

    /**
     * Set the font size.
     *
     * @param fontSize The font size.
     */
    public abstract void setFontSize(Float fontSize);

    /**
     * Set the text alignment.
     *
     * @param horizontalAlignment The horizontal alignment.
     * @param verticalAlignment   The vertical alignment.
     */
    public abstract void setTextAlignment(HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment);

    /**
     * Fills the text.
     *
     * @param text        The text to fill.
     * @param position    The position to draw the text.
     * @param maxRowWidth The maximum width of a row.
     */
    public void fillText(String text, Vector2 position, float maxRowWidth) {
        // draw request
        fillText(text, position.getX(), position.getY(), maxRowWidth);
    }

    /**
     * Fills the text.
     *
     * @param text        The text to fill.
     * @param x           The x-coordinate of the position to draw the text.
     * @param y           The y-coordinate of the position to draw the text.
     * @param maxRowWidth The maximum width of a row.
     */
    public abstract void fillText(String text, float x, float y, float maxRowWidth);

    /**
     * Strokes the text.
     *
     * @param text        The text to stroke.
     * @param position    The position to draw the text.
     * @param maxRowWidth The maximum width of a row.
     */
    public abstract void strokeText(String text, Vector2 position, float maxRowWidth);

    /**
     * Measures the text dimensions.
     *
     * @param text        The text to measure.
     * @param position    The position to draw the text.
     * @param maxRowWidth The maximum width of a row.
     * @return The text dimensions.
     */
    public abstract Size measureText(String text, Vector2 position, float maxRowWidth);

    /**
     * Set solidity.
     *
     * @param solidity The solidity.
     */
    public abstract void setSolidity(Solidity solidity);
}
