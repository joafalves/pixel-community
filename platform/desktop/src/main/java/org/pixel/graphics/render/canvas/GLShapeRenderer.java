/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import org.lwjgl.system.MemoryUtil;
import org.pixel.commons.Color;
import org.pixel.graphics.shader.opengl.GLPrimitiveShader;
import org.pixel.graphics.shader.opengl.GLVertexArrayObject;
import org.pixel.graphics.shader.opengl.GLVertexBufferObject;
import org.pixel.math.Matrix4;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Renders simple 2D shapes (rectangles, circles, etc.) using OpenGL.
 * Used by GlCanvasRenderer for shape drawing operations.
 */
public class GLShapeRenderer {

    private static final int VERTEX_SIZE = 6; // x, y, r, g, b, a
    private static final int MAX_VERTICES = 1000;

    private final GLPrimitiveShader shader;
    private final GLVertexArrayObject vao;
    private final GLVertexBufferObject vbo;
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer matrixBuffer;
    
    private int vertexCount = 0;

    /**
     * Constructor.
     */
    public GLShapeRenderer() {
        this.shader = new GLPrimitiveShader();
        this.vao = new GLVertexArrayObject();
        this.vbo = new GLVertexBufferObject();
        this.vertexBuffer = MemoryUtil.memAllocFloat(MAX_VERTICES * VERTEX_SIZE);
        this.matrixBuffer = MemoryUtil.memAllocFloat(16);

        initializeBuffers();
    }

    private void initializeBuffers() {
        vao.bind();
        vbo.bind(GL_ARRAY_BUFFER);

        // Allocate VBO (dynamic draw)
        vbo.uploadData(GL_ARRAY_BUFFER, (long) vertexBuffer.capacity() * Float.BYTES, GL_DYNAMIC_DRAW);

        // Position attribute (location 0)
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, 0);

        // Color attribute (location 1)
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, VERTEX_SIZE * Float.BYTES, 2 * Float.BYTES);

        vao.unbind();
    }

    /**
     * Render a filled rectangle.
     *
     * @param x          X position
     * @param y          Y position
     * @param width      Rectangle width
     * @param height     Rectangle height
     * @param color      Fill color
     * @param viewMatrix Camera view-projection matrix
     */
    public void fillRect(float x, float y, float width, float height, Color color, Matrix4 viewMatrix) {
        begin(viewMatrix);
        
        // Two triangles forming a rectangle
        // Triangle 1: TL, TR, BL
        addVertex(x, y, color);
        addVertex(x + width, y, color);
        addVertex(x, y + height, color);
        
        // Triangle 2: BL, TR, BR
        addVertex(x, y + height, color);
        addVertex(x + width, y, color);
        addVertex(x + width, y + height, color);
        
        flush();
    }

    /**
     * Render a filled rounded rectangle.
     *
     * @param x          X position
     * @param y          Y position
     * @param width      Rectangle width
     * @param height     Rectangle height
     * @param radius     Corner radius
     * @param color      Fill color
     * @param viewMatrix Camera view-projection matrix
     */
    public void fillRoundedRect(float x, float y, float width, float height, float radius, Color color, Matrix4 viewMatrix) {
        begin(viewMatrix);
        
        // Clamp radius to half of the smaller dimension
        radius = Math.min(radius, Math.min(width, height) / 2);
        
        // Number of segments per corner (more = smoother)
        int segments = 8;
        
        // Center rectangle (excluding corners)
        float innerX = x + radius;
        float innerY = y + radius;
        float innerWidth = width - radius * 2;
        float innerHeight = height - radius * 2;
        
        // Draw center rectangle (main body)
        addVertex(innerX, innerY, color);
        addVertex(innerX + innerWidth, innerY, color);
        addVertex(innerX, innerY + innerHeight, color);
        
        addVertex(innerX, innerY + innerHeight, color);
        addVertex(innerX + innerWidth, innerY, color);
        addVertex(innerX + innerWidth, innerY + innerHeight, color);
        
        // Draw top and bottom strips
        addVertex(innerX, y, color);
        addVertex(innerX + innerWidth, y, color);
        addVertex(innerX, innerY, color);
        
        addVertex(innerX, innerY, color);
        addVertex(innerX + innerWidth, y, color);
        addVertex(innerX + innerWidth, innerY, color);
        
        addVertex(innerX, innerY + innerHeight, color);
        addVertex(innerX + innerWidth, innerY + innerHeight, color);
        addVertex(innerX, y + height, color);
        
        addVertex(innerX, y + height, color);
        addVertex(innerX + innerWidth, innerY + innerHeight, color);
        addVertex(innerX + innerWidth, y + height, color);
        
        // Draw left and right strips
        addVertex(x, innerY, color);
        addVertex(innerX, innerY, color);
        addVertex(x, innerY + innerHeight, color);
        
        addVertex(x, innerY + innerHeight, color);
        addVertex(innerX, innerY, color);
        addVertex(innerX, innerY + innerHeight, color);
        
        addVertex(innerX + innerWidth, innerY, color);
        addVertex(x + width, innerY, color);
        addVertex(innerX + innerWidth, innerY + innerHeight, color);
        
        addVertex(innerX + innerWidth, innerY + innerHeight, color);
        addVertex(x + width, innerY, color);
        addVertex(x + width, innerY + innerHeight, color);
        
        // Draw four rounded corners using triangle fans
        // Top-left corner
        drawCorner(innerX, innerY, radius, (float) Math.PI, (float) (Math.PI * 1.5), segments, color);
        
        // Top-right corner
        drawCorner(innerX + innerWidth, innerY, radius, (float) (Math.PI * 1.5), (float) (Math.PI * 2), segments, color);
        
        // Bottom-right corner
        drawCorner(innerX + innerWidth, innerY + innerHeight, radius, 0, (float) (Math.PI * 0.5), segments, color);
        
        // Bottom-left corner
        drawCorner(innerX, innerY + innerHeight, radius, (float) (Math.PI * 0.5), (float) Math.PI, segments, color);
        
        flush();
    }

    /**
     * Draw a rounded corner as a triangle fan.
     */
    private void drawCorner(float cx, float cy, float radius, float startAngle, float endAngle, int segments, Color color) {
        float angleStep = (endAngle - startAngle) / segments;
        
        for (int i = 0; i < segments; i++) {
            float angle1 = startAngle + i * angleStep;
            float angle2 = startAngle + (i + 1) * angleStep;
            
            // Center of the arc
            addVertex(cx, cy, color);
            
            // First point on arc
            addVertex(cx + (float) Math.cos(angle1) * radius, 
                     cy + (float) Math.sin(angle1) * radius, color);
            
            // Second point on arc
            addVertex(cx + (float) Math.cos(angle2) * radius, 
                     cy + (float) Math.sin(angle2) * radius, color);
        }
    }

    /**
     * Render a line from (x1, y1) to (x2, y2) with specified width.
     * Draws the line as a rotated rectangle to support arbitrary widths.
     *
     * @param x1         Start X position
     * @param y1         Start Y position
     * @param x2         End X position
     * @param y2         End Y position
     * @param lineWidth  Line width
     * @param color      Line color
     * @param viewMatrix Camera view-projection matrix
     */
    public void strokeLine(float x1, float y1, float x2, float y2, float lineWidth, Color color, Matrix4 viewMatrix) {
        begin(viewMatrix);
        
        // Calculate line direction and perpendicular
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (length < 0.001f) {
            // Degenerate line - draw nothing or a point
            flush();
            return;
        }
        
        // Normalize direction
        dx /= length;
        dy /= length;
        
        // Perpendicular vector (rotated 90 degrees)
        float perpX = -dy * lineWidth * 0.5f;
        float perpY = dx * lineWidth * 0.5f;
        
        // Four corners of the line rectangle
        float x1a = x1 + perpX;
        float y1a = y1 + perpY;
        float x1b = x1 - perpX;
        float y1b = y1 - perpY;
        float x2a = x2 + perpX;
        float y2a = y2 + perpY;
        float x2b = x2 - perpX;
        float y2b = y2 - perpY;
        
        // Two triangles forming the line rectangle
        // Triangle 1
        addVertex(x1a, y1a, color);
        addVertex(x2a, y2a, color);
        addVertex(x1b, y1b, color);
        
        // Triangle 2
        addVertex(x1b, y1b, color);
        addVertex(x2a, y2a, color);
        addVertex(x2b, y2b, color);
        
        flush();
    }

    /**
     * Render a point (filled circle) at the specified position.
     *
     * @param x          Center X position
     * @param y          Center Y position
     * @param size       Point size (diameter)
     * @param color      Point color
     * @param viewMatrix Camera view-projection matrix
     */
    public void fillPoint(float x, float y, float size, Color color, Matrix4 viewMatrix) {
        // A point is just a small filled circle
        fillCircle(x, y, size * 0.5f, color, viewMatrix);
    }

    /**
     * Render a filled circle.
     *
     * @param cx         Center X position
     * @param cy         Center Y position
     * @param radius     Circle radius
     * @param color      Fill color
     * @param viewMatrix Camera view-projection matrix
     */
    public void fillCircle(float cx, float cy, float radius, Color color, Matrix4 viewMatrix) {
        begin(viewMatrix);
        
        int segments = Math.max(8, (int) (radius * 0.5f)); // More segments for larger circles
        segments = Math.min(segments, 32); // Cap at 32 segments
        
        float angleStep = (float) (Math.PI * 2.0 / segments);
        
        for (int i = 0; i < segments; i++) {
            float angle1 = i * angleStep;
            float angle2 = (i + 1) * angleStep;
            
            // Center of circle
            addVertex(cx, cy, color);
            
            // First point on circumference
            addVertex(cx + (float) Math.cos(angle1) * radius,
                     cy + (float) Math.sin(angle1) * radius, color);
            
            // Second point on circumference
            addVertex(cx + (float) Math.cos(angle2) * radius,
                     cy + (float) Math.sin(angle2) * radius, color);
        }
        
        flush();
    }

    /**
     * Begin rendering with a specific view matrix.
     */
    private void begin(Matrix4 viewMatrix) {
        shader.bind();
        vao.bind();

        // Upload view matrix
        matrixBuffer.clear();
        viewMatrix.writeBuffer(matrixBuffer);
        glUniformMatrix4fv(shader.getUniformLocation("uMatrix"), false, matrixBuffer);

        // Enable blending
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        vertexBuffer.clear();
        vertexCount = 0;
    }

    /**
     * Add a vertex to the buffer.
     */
    private void addVertex(float x, float y, Color color) {
        vertexBuffer.put(x);
        vertexBuffer.put(y);
        vertexBuffer.put(color.getRed());
        vertexBuffer.put(color.getGreen());
        vertexBuffer.put(color.getBlue());
        vertexBuffer.put(color.getAlpha());
        vertexCount++;

        // Auto-flush if buffer is full
        if (vertexCount >= MAX_VERTICES) {
            flush();
        }
    }

    /**
     * Flush all pending vertices to GPU.
     */
    private void flush() {
        if (vertexCount == 0) {
            return;
        }

        vertexBuffer.flip();

        vbo.bind(GL_ARRAY_BUFFER);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);

        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        vertexBuffer.clear();
        vertexCount = 0;
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
