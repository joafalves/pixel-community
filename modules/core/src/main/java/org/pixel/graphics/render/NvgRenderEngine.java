/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.pixel.commons.attribute.HorizontalAlignment;
import org.pixel.commons.attribute.Solidity;
import org.pixel.commons.attribute.VerticalAlignment;
import org.pixel.graphics.Color;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Size;
import org.pixel.math.Vector2;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;

public class NvgRenderEngine extends RenderEngine2D {

    public static final NVGColor nvgColor1 = NVGColor.create();
    public static final NVGColor nvgColor2 = NVGColor.create();
    public static final NVGPaint nvgPaint1 = NVGPaint.create();
    public static final FloatBuffer buffer4 = BufferUtils.createFloatBuffer(4);

    protected HashMap<String, Integer> fontMapping;
    protected HashMap<String, ByteBuffer> fontBufferMapping;

    private String prevFontName = null;

    private final long ctx;

    /**
     * Constructor.
     *
     * @param viewportWidth The width of the viewport.
     * @param viewportHeight The height of the viewport.
     */
    public NvgRenderEngine(int viewportWidth, int viewportHeight) {
        super(new Rectangle(0, 0, viewportWidth, viewportHeight));
        this.fontMapping = new HashMap<>();
        this.fontBufferMapping = new HashMap<>();
        this.ctx = nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES);
    }

    @Override
    public void begin() {
        nvgBeginFrame(ctx, viewportDimensions.getWidth(), viewportDimensions.getHeight(), pixelRatio);
    }

    @Override
    public void end() {
        nvgEndFrame(ctx);
    }

    @Override
    public void addFont(ByteBuffer fontData, String fontName) {
        if (fontMapping.containsKey(fontName)) {
            return;
        }

        int fontRef = nvgCreateFontMem(ctx, fontName, fontData, 0);
        if (fontRef >= 0) {
            fontMapping.put(fontName, fontRef);
            fontBufferMapping.put(fontName, fontData);
        }
    }

    @Override
    public void push() {
        nvgSave(ctx);
    }

    @Override
    public void pop() {
        nvgRestore(ctx);
    }

    @Override
    public void transform(Matrix4 viewMatrix) {
        float[][] matrixValues = viewMatrix.toUnsafeArray();
        nvgTransform(ctx, matrixValues[0][0], matrixValues[1][0], matrixValues[0][1], matrixValues[1][1],
                matrixValues[0][2], matrixValues[1][2]);
    }

    @Override
    public void translate(float x, float y) {
        nvgTranslate(ctx, x, y);
    }

    @Override
    public void rotate(float angle) {
        nvgRotate(ctx, angle);
    }

    @Override
    public void scale(float x, float y) {
        nvgScale(ctx, x, y);
    }

    @Override
    public void beginPath() {
        nvgBeginPath(ctx);
    }

    @Override
    public void roundedRectangle(float x, float y, float width, float height, float cornerRadius) {
        nvgRoundedRect(ctx, x, y, width, height, cornerRadius);
    }

    @Override
    public void rectangle(float x, float y, float width, float height) {
        nvgRect(ctx, x, y, width, height);
    }

    @Override
    public void linearGradient(float sx, float sy, float ex, float ey, Color startColor, Color endColor) {
        nvgLinearGradient(ctx, sx, sy, ex, ey, set(startColor, nvgColor1), set(endColor, nvgColor2), nvgPaint1);
    }

    @Override
    public void boxGradient(float x, float y, float width, float height, float radius, float feather, Color startColor,
            Color endColor) {
        nvgBoxGradient(ctx, x, y, width, height, radius, feather, set(startColor, nvgColor1), set(endColor, nvgColor2),
                nvgPaint1);
    }

    @Override
    public void fillColor(Color color) {
        nvgFillColor(ctx, set(color, nvgColor1));
    }

    @Override
    public void strokeWidth(float width) {
        nvgStrokeWidth(ctx, width);
    }

    @Override
    public void strokeColor(Color color) {
        nvgStrokeColor(ctx, set(color, nvgColor1));
    }

    @Override
    public void fillPaint() {
        nvgFillPaint(ctx, nvgPaint1);
    }

    @Override
    public void stroke() {
        nvgStroke(ctx);
    }

    @Override
    public void fill() {
        nvgFill(ctx);
    }

    @Override
    public void scissor(float x, float y, float width, float height) {
        nvgScissor(ctx, x, y, width, height);
    }

    @Override
    public void setFontName(String fontName) {
        if (!fontName.equals(prevFontName)) {
            nvgFontFace(ctx, fontName);
            prevFontName = fontName;
        }
    }

    @Override
    public void setFontSize(Float fontSize) {
        nvgFontSize(ctx, fontSize);
    }

    @Override
    public void setTextAlignment(HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
        nvgTextAlign(ctx, convert(horizontalAlignment) | convert(verticalAlignment));
    }

    @Override
    public void fillText(String text, float x, float y, float maxRowWidth) {
        nvgTextBox(ctx, x, y, maxRowWidth, text);
    }

    @Override
    public void strokeText(String text, Vector2 position, float maxRowWidth) {
        // not yet implemented!
    }

    @Override
    public Size measureText(String text, Vector2 position, float maxRowWidth) {
        // compute
        nvgTextBoxBounds(ctx, position.getX(), position.getY(), maxRowWidth, text, buffer4);

        return new Size(buffer4.get(2) - buffer4.get(0), buffer4.get(3) - buffer4.get(1));
    }

    @Override
    public void moveTo(float x, float y) {
        nvgMoveTo(ctx, x, y);
    }

    @Override
    public void lineTo(float x, float y) {
        nvgLineTo(ctx, x, y);
    }

    @Override
    public void bezierCurveTo(float cp1x, float cp1y, float cp2x, float cp2y, float x, float y) {
        nvgBezierTo(ctx, cp1x, cp1y, cp2x, cp2y, x, y);
    }

    @Override
    public void quadraticCurveTo(float cpx, float cpy, float x, float y) {
        nvgQuadTo(ctx, cpx, cpy, x, y);
    }

    @Override
    public void circle(float x, float y, float radius) {
        nvgCircle(ctx, x, y, radius);
    }

    @Override
    public void endPath() {
        nvgClosePath(ctx);
    }

    @Override
    public void setSolidity(Solidity solidity) {
        nvgPathWinding(ctx, convert(solidity));
    }

    @Override
    public void dispose() {
        nvgDelete(ctx);
    }

    //region helper methods

    private int convert(Solidity solidity) {
        switch (solidity) {
            case HOLE:
                return NVG_HOLE;

            default:
            case SOLID:
                return NVG_SOLID;

        }
    }

    private int convert(HorizontalAlignment alignment) {
        switch (alignment) {
            case LEFT:
                return NVG_ALIGN_LEFT;
            case CENTER:
                return NVG_ALIGN_CENTER;
            case RIGHT:
                return NVG_ALIGN_RIGHT;
        }

        return 0;
    }

    private int convert(VerticalAlignment alignment) {
        switch (alignment) {
            case BOTTOM:
                return NVG_ALIGN_BOTTOM;
            case MIDDLE:
                return NVG_ALIGN_MIDDLE;
            case TOP:
                return NVG_ALIGN_TOP;
        }

        return 0;
    }

    private NVGColor set(Color color, NVGColor out) {
        out.r(color.getRed());
        out.g(color.getGreen());
        out.b(color.getBlue());
        out.a(color.getAlpha());

        return out;
    }

    //endregion
}
