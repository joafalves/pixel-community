/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

import org.pixel.commons.model.HorizontalAlignment;
import org.pixel.commons.model.Solidity;
import org.pixel.commons.model.VerticalAlignment;
import org.pixel.graphics.Color;
import org.pixel.graphics.shader.VertexArrayObject;
import org.pixel.graphics.shader.VertexBufferObject;
import org.pixel.graphics.shader.standard.PixelRenderEngineShader;
import org.pixel.math.FSize;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.glEnable;

public class PxRenderEngine extends RenderEngine2D {

    private final PixelRenderEngineShader shader;
    private final Matrix4 viewMatrix;
    private final VertexArrayObject vao;
    private final VertexBufferObject vbo;

    /**
     * Constructor
     *
     * @param viewportDimensions
     */
    public PxRenderEngine(Rectangle viewportDimensions) {
        super(viewportDimensions);
        this.shader = new PixelRenderEngineShader();
        this.viewMatrix = new Matrix4();
        this.vao = new VertexArrayObject();
        this.vbo = new VertexBufferObject();
    }

    /**
     * Begin render frame
     */
    @Override
    public void begin() {
        // reset visualization matrix:
        this.viewMatrix.setOrthographic(viewportDimensions.getX(), viewportDimensions.getX() +
                        viewportDimensions.getWidth(), viewportDimensions.getY() + viewportDimensions.getHeight(),
                viewportDimensions.getY(), 0.0f, 1.0f);

        glEnable(GL_STENCIL_TEST);
    }

    /**
     * End render frame
     */
    @Override
    public void end() {

    }

    /**
     * @param fontData
     * @param fontName
     */
    @Override
    public void addFont(ByteBuffer fontData, String fontName) {

    }

    /**
     * Push current state into the render stack
     */
    @Override
    public void push() {

    }

    /**
     * Pop current state from the render stack, restoring the previous point
     */
    @Override
    public void pop() {

    }

    /**
     * @param x
     * @param y
     */
    @Override
    public void translate(float x, float y) {
        viewMatrix.translate(x, y, 0);
    }

    /**
     *
     */
    @Override
    public void beginPath() {

    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @param cornerRadius
     */
    @Override
    public void roundedRectangle(float x, float y, float width, float height, float cornerRadius) {

    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @Override
    public void rectangle(float x, float y, float width, float height) {

    }

    /**
     * @param sx
     * @param sy
     * @param ex
     * @param ey
     * @param startColor
     * @param endColor
     */
    @Override
    public void linearGradient(float sx, float sy, float ex, float ey, Color startColor, Color endColor) {

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
    @Override
    public void boxGradient(float x, float y, float width, float height, float radius, float feather, Color startColor, Color endColor) {

    }

    /**
     * @param color
     */
    @Override
    public void fillColor(Color color) {

    }

    /**
     * @param width
     */
    @Override
    public void strokeWidth(float width) {

    }

    /**
     * @param color
     */
    @Override
    public void strokeColor(Color color) {

    }

    /**
     * @param x
     * @param y
     */
    @Override
    public void moveTo(float x, float y) {

    }

    /**
     * @param x
     * @param y
     */
    @Override
    public void lineTo(float x, float y) {

    }

    /**
     * @param cp1x
     * @param cp1y
     * @param cp2x
     * @param cp2y
     * @param x
     * @param y
     */
    @Override
    public void bezierCurveTo(float cp1x, float cp1y, float cp2x, float cp2y, float x, float y) {

    }

    /**
     * @param cpx
     * @param cpy
     * @param x
     * @param y
     */
    @Override
    public void quadraticCurveTo(float cpx, float cpy, float x, float y) {

    }

    /**
     * @param x
     * @param y
     * @param radius
     */
    @Override
    public void circle(float x, float y, float radius) {

    }

    /**
     *
     */
    @Override
    public void closePath() {

    }

    /**
     *
     */
    @Override
    public void fillPaint() {

    }

    /**
     *
     */
    @Override
    public void stroke() {

    }

    /**
     *
     */
    @Override
    public void fill() {

    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     */
    @Override
    public void scissor(float x, float y, float width, float height) {

    }

    /**
     * @param fontName
     */
    @Override
    public void setFontName(String fontName) {

    }

    /**
     * @param fontSize
     */
    @Override
    public void setFontSize(Float fontSize) {

    }

    /**
     * @param horizontalAlignment
     * @param verticalAlignment
     */
    @Override
    public void setTextAlignment(HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {

    }

    /**
     * @param text
     * @param x
     * @param y
     * @param maxRowWidth
     */
    @Override
    public void fillText(String text, float x, float y, float maxRowWidth) {

    }

    /**
     * @param text
     * @param position
     * @param maxRowWidth
     */
    @Override
    public void strokeText(String text, Vector2 position, float maxRowWidth) {

    }

    /**
     * @param text
     * @param position
     * @param maxRowWidth
     * @return
     */
    @Override
    public FSize measureText(String text, Vector2 position, float maxRowWidth) {
        return null;
    }

    /**
     * @param solidity
     */
    @Override
    public void setSolidity(Solidity solidity) {

    }

    /**
     * Dispose function
     */
    @Override
    public void dispose() {
        vbo.dispose();
        vao.dispose();
        shader.dispose();
    }
}
