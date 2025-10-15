/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import org.lwjgl.system.MemoryUtil;
import org.pixel.commons.Color;
import org.pixel.graphics.render.SdfShapeRenderer;
import org.pixel.graphics.shader.opengl.GLSdfShapeShader;
import org.pixel.graphics.shader.opengl.GLVertexArrayObject;
import org.pixel.graphics.shader.opengl.GLVertexBufferObject;
import org.pixel.math.Matrix4;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * OpenGL implementation of SdfShapeRenderer.
 * Renders shapes using signed distance fields in the fragment shader,
 * requiring only 2 triangles (1 quad) per shape regardless of complexity.
 */
public class GlSdfShapeRenderer implements SdfShapeRenderer {

    private static final int VERTEX_SIZE = 4; // x, y, u, v
    private static final int SHAPE_ROUNDED_RECT = 0;
    private static final int SHAPE_CIRCLE = 1;
    
    private final GLSdfShapeShader shader;
    private final GLVertexArrayObject vao;
    private final GLVertexBufferObject vbo;
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer matrixBuffer;

    /**
     * Constructor.
     */
    public GlSdfShapeRenderer() {
        this.shader = new GLSdfShapeShader();
        this.vao = new GLVertexArrayObject();
        this.vbo = new GLVertexBufferObject();
        this.vertexBuffer = MemoryUtil.memAllocFloat(6 * VERTEX_SIZE); // 6 vertices for 2 triangles
        this.matrixBuffer = MemoryUtil.memAllocFloat(16);

        initializeBuffers();
    }

    private void initializeBuffers() {
        vao.bind();
        vbo.bind(GL_ARRAY_BUFFER);

        // Allocate VBO
        vbo.uploadData(GL_ARRAY_BUFFER, (long) vertexBuffer.capacity() * Float.BYTES, GL_DYNAMIC_DRAW);

        // Position attribute (location 0)
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, 0);

        // TexCoord attribute (location 1)
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, 2 * Float.BYTES);

        vao.unbind();
    }

    /**
     * Render a filled rounded rectangle using SDF.
     * Only requires 2 triangles regardless of corner radius!
     *
     * @param x          X position
     * @param y          Y position
     * @param width      Rectangle width
     * @param height     Rectangle height
     * @param radius     Corner radius (0 = sharp corners)
     * @param color      Fill color
     * @param transform  Transform matrix (combines local transform + view matrix)
     */
    @Override
    public void fillRoundedRect(float x, float y, float width, float height, float radius, Color color, Matrix4 transform) {
        renderShape(x, y, width, height, radius, color, transform, 0.0f, SHAPE_ROUNDED_RECT);
    }

    /**
     * Render a stroked rounded rectangle using SDF.
     *
     * @param x           X position
     * @param y           Y position
     * @param width       Rectangle width
     * @param height      Rectangle height
     * @param radius      Corner radius (0 = sharp corners)
     * @param strokeWidth Stroke width
     * @param color       Stroke color
     * @param transform   Transform matrix
     */
    @Override
    public void strokeRoundedRect(float x, float y, float width, float height, float radius, float strokeWidth, Color color, Matrix4 transform) {
        renderShape(x, y, width, height, radius, color, transform, strokeWidth, SHAPE_ROUNDED_RECT);
    }

    /**
     * Render a filled circle using SDF.
     *
     * @param x         Center X position
     * @param y         Center Y position
     * @param radius    Circle radius
     * @param color     Fill color
     * @param transform Transform matrix
     */
    @Override
    public void fillCircle(float x, float y, float radius, Color color, Matrix4 transform) {
        // For circle, we render a square quad and the shader draws the circle
        float size = radius * 2;
        float left = x - radius;
        float top = y - radius;
        renderShape(left, top, size, size, radius, color, transform, 0.0f, SHAPE_CIRCLE);
    }

    /**
     * Render a stroked circle using SDF.
     *
     * @param x           Center X position
     * @param y           Center Y position
     * @param radius      Circle radius
     * @param strokeWidth Stroke width
     * @param color       Stroke color
     * @param transform   Transform matrix
     */
    @Override
    public void strokeCircle(float x, float y, float radius, float strokeWidth, Color color, Matrix4 transform) {
        float size = radius * 2;
        float left = x - radius;
        float top = y - radius;
        renderShape(left, top, size, size, radius, color, transform, strokeWidth, SHAPE_CIRCLE);
    }

    /**
     * Core rendering method for all SDF shapes.
     */
    private void renderShape(float x, float y, float width, float height, float radius, 
                           Color color, Matrix4 viewMatrix, float strokeWidth, int shapeType) {
        shader.bind();
        vao.bind();

        // For stroked shapes, we need to expand the rendering quad to accommodate
        // the stroke extending outward. The stroke is centered on the edge, so we
        // need to add strokeWidth/2 padding on all sides, plus extra for anti-aliasing
        float padding = strokeWidth > 0 ? strokeWidth / 2 + 2 : 2; // Always add 2px for AA
        float quadX = x - padding;
        float quadY = y - padding;
        float quadWidth = width + padding * 2;
        float quadHeight = height + padding * 2;

        // Upload uniforms
        matrixBuffer.clear();
        viewMatrix.writeBuffer(matrixBuffer);
        glUniformMatrix4fv(shader.getUniformLocation("uMatrix"), false, matrixBuffer);

        glUniform4f(shader.getUniformLocation("uColor"),
            color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        
        // Pass the ORIGINAL shape size for SDF calculation
        glUniform2f(shader.getUniformLocation("uSize"), width, height);
        
        // Pass the EXPANDED quad size for texture coordinate mapping
        glUniform2f(shader.getUniformLocation("uQuadSize"), quadWidth, quadHeight);
        
        // Radius stays the same (absolute measurement)
        glUniform1f(shader.getUniformLocation("uRadius"), radius);
        
        glUniform1f(shader.getUniformLocation("uSmoothness"), 1.0f); // Smooth anti-aliasing
        glUniform1f(shader.getUniformLocation("uStrokeWidth"), strokeWidth);
        glUniform1i(shader.getUniformLocation("uShapeType"), shapeType);

        // Enable blending
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Build quad vertices using the EXPANDED size
        vertexBuffer.clear();
        
        // Triangle 1: TL, TR, BL
        addVertex(quadX, quadY, 0, 0);
        addVertex(quadX + quadWidth, quadY, 1, 0);
        addVertex(quadX, quadY + quadHeight, 0, 1);
        
        // Triangle 2: BL, TR, BR
        addVertex(quadX, quadY + quadHeight, 0, 1);
        addVertex(quadX + quadWidth, quadY, 1, 0);
        addVertex(quadX + quadWidth, quadY + quadHeight, 1, 1);

        vertexBuffer.flip();

        // Upload and draw
        vbo.bind(GL_ARRAY_BUFFER);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        glDisable(GL_BLEND);
        vao.unbind();
    }

    private void addVertex(float x, float y, float u, float v) {
        vertexBuffer.put(x);
        vertexBuffer.put(y);
        vertexBuffer.put(u);
        vertexBuffer.put(v);
    }

    /**
     * Dispose resources.
     */
    public void dispose() {
        shader.dispose();
        vao.dispose();
        vbo.dispose();
        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(matrixBuffer);
    }
}
