/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;
import org.pixel.graphics.Color;
import org.pixel.graphics.shader.ShaderManager;
import org.pixel.graphics.shader.VertexBufferObject;
import org.pixel.graphics.shader.standard.PrimitiveShader;
import org.pixel.math.Matrix4;
import org.pixel.math.Vector2;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

@Deprecated
public class PrimitiveBatch extends DrawBatch {

    //region private properties

    private static final int BUFFER_LENGTH_UNIT = 250;

    private static final int POINT_UNIT_LENGTH = 6;
    private static final int POINT_STRIDE = Float.BYTES * POINT_UNIT_LENGTH;

    private static final int LINE_UNIT_LENGTH = 6;
    private static final int LINE_STRIDE = Float.BYTES * LINE_UNIT_LENGTH;

    private PrimitiveShader shader;

    private VertexBufferObject vertexBuffer;
    private FloatBuffer lineBuffer;
    private FloatBuffer pointBuffer;
    private FloatBuffer matrixBuffer;
    private Matrix4 viewMatrix;

    private float lineWidth = 1.0f;
    private float pointSize = 1.0f;

    //endregion

    //region constructors

    public PrimitiveBatch() {
        shader = new PrimitiveShader();
        vertexBuffer = new VertexBufferObject();
        lineBuffer = MemoryUtil.memAllocFloat(BUFFER_LENGTH_UNIT * LINE_UNIT_LENGTH);
        pointBuffer = MemoryUtil.memAllocFloat(BUFFER_LENGTH_UNIT * POINT_UNIT_LENGTH);
        matrixBuffer = MemoryUtil.memAllocFloat(4 * 4);
    }

    //endregion

    //region private methods

    private void prepareFlush() {
        // use shader
        ShaderManager.useShader(shader);

        // global
        glLineWidth(lineWidth);
        glPointSize(pointSize);

        // bind buffer
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.getID());

        // apply camera matrix
        matrixBuffer.clear();
        viewMatrix.writeBuffer(matrixBuffer);
        GL20.glUniformMatrix4fv(shader.getUniformLocation("uMatrix"), false, matrixBuffer);
    }

    private void flushPoints() {
        if (pointBuffer.position() == 0) {
            return;
        }

        int vertexCount = pointBuffer.position() / POINT_UNIT_LENGTH;
        int aVertexPosition = shader.getAttributeLocation("aVertexPosition");
        int aVertexColorPosition = shader.getAttributeLocation("aVertexColorPosition");

        pointBuffer.flip();
        vertexBuffer.uploadData(GL_ARRAY_BUFFER, pointBuffer, GL_STATIC_DRAW);

        glEnableVertexAttribArray(aVertexPosition);
        glVertexAttribPointer(aVertexPosition, 2, GL_FLOAT, false, POINT_STRIDE, 0);

        glEnableVertexAttribArray(aVertexColorPosition);
        glVertexAttribPointer(aVertexColorPosition, 4, GL_FLOAT, true, POINT_STRIDE, 2 * Float.BYTES);

        glDrawArrays(GL_POINTS, 0, vertexCount);
    }

    private void flushLines() {
        if (lineBuffer.position() == 0) {
            return;
        }

        int vertexCount = lineBuffer.position() / LINE_UNIT_LENGTH;
        int aVertexPosition = shader.getAttributeLocation("aVertexPosition");
        int aVertexColorPosition = shader.getAttributeLocation("aVertexColorPosition");

        lineBuffer.flip();
        vertexBuffer.uploadData(GL_ARRAY_BUFFER, lineBuffer, GL_STATIC_DRAW);

        glEnableVertexAttribArray(aVertexPosition);
        glVertexAttribPointer(aVertexPosition, 2, GL_FLOAT, false, LINE_STRIDE, 0);

        glEnableVertexAttribArray(aVertexColorPosition);
        glVertexAttribPointer(aVertexColorPosition, 4, GL_FLOAT, true, LINE_STRIDE, 2 * Float.BYTES);

        glDrawArrays(GL_LINES, 0, vertexCount * 2);
    }

    //endregion

    //region public methods

    public void drawPoint(Vector2 pos, Color color) {
        // point 1
        pointBuffer.put(pos.getX());
        pointBuffer.put(pos.getY());
        pointBuffer.put(color.getRed());
        pointBuffer.put(color.getGreen());
        pointBuffer.put(color.getBlue());
        pointBuffer.put(color.getAlpha());

        // flush needed?
        if (pointBuffer.position() >= pointBuffer.capacity()) {
            prepareFlush();
            flushPoints();
            pointBuffer.clear();
        }
    }

    public void drawLine(Vector2 pos1, Vector2 pos2, Color color) {
        drawLine(pos1, pos2, color, color);
    }

    public void drawLine(Vector2 pos1, Vector2 pos2, Color color1, Color color2) {
        // point 1
        lineBuffer.put(pos1.getX());
        lineBuffer.put(pos1.getY());
        lineBuffer.put(color1.getRed());
        lineBuffer.put(color1.getGreen());
        lineBuffer.put(color1.getBlue());
        lineBuffer.put(color1.getAlpha());
        // point 2
        lineBuffer.put(pos2.getX());
        lineBuffer.put(pos2.getY());
        lineBuffer.put(color2.getRed());
        lineBuffer.put(color2.getGreen());
        lineBuffer.put(color2.getBlue());
        lineBuffer.put(color2.getAlpha());

        // flush needed?
        if (lineBuffer.position() >= lineBuffer.capacity()) {
            prepareFlush();
            flushLines();
            lineBuffer.clear();
        }
    }

    @Override
    public void begin(Matrix4 viewMatrix) {
        this.viewMatrix = viewMatrix;
        this.lineBuffer.clear();
        this.pointBuffer.clear();
    }

    @Override
    public void end() {
        prepareFlush();
        flushLines();
        flushPoints();
    }

    @Override
    public void dispose() {
        shader.dispose();
        vertexBuffer.dispose();
    }

    //endregion

    //region getters & setters

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public float getPointSize() {
        return pointSize;
    }

    public void setPointSize(float pointSize) {
        this.pointSize = pointSize;
    }

    //endregion
}
