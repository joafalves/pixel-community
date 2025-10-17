/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas;

import org.lwjgl.system.MemoryUtil;
import org.pixel.commons.Color;
import org.pixel.graphics.render.canvas.text.SdfFont;
import org.pixel.graphics.shader.opengl.GLSdfBatchShader;
import org.pixel.graphics.shader.opengl.GLVertexArrayObject;
import org.pixel.graphics.shader.opengl.GLVertexBufferObject;
import org.pixel.math.Matrix4;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Unified SDF-based batch renderer that handles all canvas drawing operations:
 * - Shapes (rectangles, circles, rounded rects)
 * - Lines and points
 * - Text (SDF font glyphs)
 * 
 * Everything is batched together and rendered in submission order, ensuring perfect
 * draw order while maximizing performance with minimal draw calls.
 */
public class GLSdfBatchRenderer {

    // Shape type constants (must match shader)
    public static final int SHAPE_ROUNDED_RECT = 0;
    public static final int SHAPE_CIRCLE = 1;
    public static final int SHAPE_LINE = 2;
    public static final int SHAPE_POINT = 3;
    public static final int SHAPE_TEXT_GLYPH = 4;
    public static final int SHAPE_TRIANGLE = 5; // Raw filled triangle (no SDF)
    public static final int SHAPE_TEXTURED_QUAD = 6; // Textured image quad

    // Vertex layout: position(2) + texCoord(2) + color(4) + shapeData(4) + quadSize(2) + shapeType(1) + textureId(1) = 16 floats
    private static final int VERTEX_SIZE = 16;
    private static final int MAX_VERTICES = 60000; // 10,000 quads (60,000 vertices)
    
    private final GLSdfBatchShader shader;
    private final GLVertexArrayObject vao;
    private final GLVertexBufferObject vbo;
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer matrixBuffer;
    
    private int vertexCount = 0;
    private boolean begun = false;
    private Matrix4 currentViewMatrix;
    private Matrix4 currentLocalTransform; // Local transform applied on CPU
    private SdfFont currentFont; // Track current font for texture binding
    
    // Multi-texture support - textures to bind during flush
    private final int[] activeTextures = new int[8]; // Max 8 textures
    private int activeTextureCount = 0;
    
    // Culling support
    private float cullingMinX, cullingMinY, cullingMaxX, cullingMaxY;
    private boolean cullingEnabled = true;

    /**
     * Constructor.
     */
    public GLSdfBatchRenderer() {
        this.shader = new GLSdfBatchShader();
        this.vao = new GLVertexArrayObject();
        this.vbo = new GLVertexBufferObject();
        this.vertexBuffer = MemoryUtil.memAllocFloat(MAX_VERTICES * VERTEX_SIZE);
        this.matrixBuffer = MemoryUtil.memAllocFloat(16);

        initializeBuffers();
    }

    private void initializeBuffers() {
        vao.bind();
        vbo.bind(GL_ARRAY_BUFFER);

        // Allocate VBO (dynamic draw for batching)
        vbo.uploadData(GL_ARRAY_BUFFER, (long) vertexBuffer.capacity() * Float.BYTES, GL_DYNAMIC_DRAW);

        int stride = VERTEX_SIZE * Float.BYTES;
        int offset = 0;

        // Position attribute (location 0) - vec2
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, offset);
        offset += 2 * Float.BYTES;

        // TexCoord attribute (location 1) - vec2
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, offset);
        offset += 2 * Float.BYTES;

        // Color attribute (location 2) - vec4
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 4, GL_FLOAT, false, stride, offset);
        offset += 4 * Float.BYTES;

        // ShapeData attribute (location 3) - vec4
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 4, GL_FLOAT, false, stride, offset);
        offset += 4 * Float.BYTES;

        // QuadSize attribute (location 4) - vec2
        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 2, GL_FLOAT, false, stride, offset);
        offset += 2 * Float.BYTES;

        // ShapeType attribute (location 5) - float
        glEnableVertexAttribArray(5);
        glVertexAttribPointer(5, 1, GL_FLOAT, false, stride, offset);
        offset += Float.BYTES;

        // TextureId attribute (location 6) - float
        glEnableVertexAttribArray(6);
        glVertexAttribPointer(6, 1, GL_FLOAT, false, stride, offset);

        vao.unbind();
    }

    /**
     * Begin a batched rendering session.
     * 
     * @param viewMatrix The view-projection matrix for the shader
     * @param localTransform The local transform to apply to vertices on CPU
     */
    public void begin(Matrix4 viewMatrix, Matrix4 localTransform) {
        if (begun) {
            throw new IllegalStateException("GlSdfBatchRenderer.begin() called twice without end()");
        }
        
        this.currentViewMatrix = viewMatrix;
        this.currentLocalTransform = localTransform;
        this.currentFont = null;
        begun = true;
        
        // Extract viewport bounds from orthographic projection matrix for culling
        extractViewportBounds(viewMatrix);
        
        shader.bind();
        vao.bind();

        // Upload view matrix only (local transforms applied on CPU)
        matrixBuffer.clear();
        viewMatrix.writeBuffer(matrixBuffer);
        glUniformMatrix4fv(shader.getUniformLocation("uViewMatrix"), false, matrixBuffer);

        // Set global smoothness for anti-aliasing
        glUniform1f(shader.getUniformLocation("uSmoothness"), 1.0f);
        
        // Set text edge threshold for SDF text rendering
        glUniform1f(shader.getUniformLocation("uTextEdge"), GLSdfConstants.SDF_TEXT_EDGE_THRESHOLD);

        // Set up texture unit indices for texture array (units 1-8)
        int texturesLocation = shader.getUniformLocation("uTextures");
        if (texturesLocation >= 0) {
            int[] textureUnits = {1, 2, 3, 4, 5, 6, 7, 8}; // Texture units 1-8 (0 is for text atlas)
            glUniform1iv(texturesLocation, textureUnits);
        }

        // Enable blending
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        vertexBuffer.clear();
        vertexCount = 0;
    }
    
    /**
     * Update the local transform without flushing the batch.
     * This allows transform changes without breaking the batch!
     * 
     * @param localTransform The new local transform
     */
    public void setLocalTransform(Matrix4 localTransform) {
        this.currentLocalTransform = localTransform;
    }

    /**
     * Set active textures for multi-texture batching.
     * These textures will be bound to texture units 0-N during flush.
     * 
     * @param textureIds Array of OpenGL texture IDs to bind
     * @param count Number of textures in the array
     */
    public void setActiveTextures(int[] textureIds, int count) {
        this.activeTextureCount = Math.min(count, activeTextures.length);
        System.arraycopy(textureIds, 0, activeTextures, 0, activeTextureCount);
    }

    /**
     * End the batched rendering session and flush all pending geometry.
     */
    public void end() {
        if (!begun) {
            throw new IllegalStateException("GlSdfBatchRenderer.end() called without begin()");
        }
        
        flush();
        begun = false;
        currentFont = null;
    }

    /**
     * Flush all batched geometry to the GPU.
     */
    public void flush() {
        if (vertexCount == 0) {
            return;
        }

        // Bind active textures to texture units 1-N (unit 0 is reserved for text atlas)
        for (int i = 0; i < activeTextureCount; i++) {
            glActiveTexture(GL_TEXTURE1 + i); // Start from unit 1 (unit 0 is for text atlas)
            glBindTexture(GL_TEXTURE_2D, activeTextures[i]);
        }

        vertexBuffer.flip();

        vbo.bind(GL_ARRAY_BUFFER);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        vertexBuffer.clear();
        vertexCount = 0;
    }

    /**
     * Check if we need to flush before adding more vertices.
     */
    private void checkFlush(int verticesNeeded) {
        if (vertexCount + verticesNeeded > MAX_VERTICES) {
            flush();
        }
    }

    private void addVertex(float x, float y, float u, float v, Color color, 
                          float sd1, float sd2, float sd3, float sd4,
                          float quadWidth, float quadHeight,
                          int shapeType, int textureId) {
        // Apply local transform to vertex position on CPU
        // Matrix is column-major: m[col][row]
        // For 2D: transformed_x = m[0][0]*x + m[1][0]*y + m[3][0]
        //         transformed_y = m[0][1]*x + m[1][1]*y + m[3][1]
        float[][] mat = currentLocalTransform.toUnsafeArray();
        float transformedX = mat[0][0] * x + mat[1][0] * y + mat[3][0];
        float transformedY = mat[0][1] * x + mat[1][1] * y + mat[3][1];
        
        // For shapes, we need to transform the quad size for proper SDF rendering
        // For text, quad size transformation is NOT needed (texture coords are fixed)
        float transformedQuadWidth = quadWidth;
        float transformedQuadHeight = quadHeight;
        
        if (shapeType != SHAPE_TEXT_GLYPH) {
            // Extract scale from matrix: scale_x = length of first column, scale_y = length of second column
            float scaleX = (float) Math.sqrt(mat[0][0] * mat[0][0] + mat[0][1] * mat[0][1]);
            float scaleY = (float) Math.sqrt(mat[1][0] * mat[1][0] + mat[1][1] * mat[1][1]);
            transformedQuadWidth = quadWidth * scaleX;
            transformedQuadHeight = quadHeight * scaleY;
        }
        
        vertexBuffer.put(transformedX);
        vertexBuffer.put(transformedY);
        vertexBuffer.put(u);
        vertexBuffer.put(v);
        vertexBuffer.put(color.getRed());
        vertexBuffer.put(color.getGreen());
        vertexBuffer.put(color.getBlue());
        vertexBuffer.put(color.getAlpha());
        vertexBuffer.put(sd1);
        vertexBuffer.put(sd2);
        vertexBuffer.put(sd3);
        vertexBuffer.put(sd4);
        vertexBuffer.put(transformedQuadWidth);
        vertexBuffer.put(transformedQuadHeight);
        vertexBuffer.put((float) shapeType);
        vertexBuffer.put((float) textureId);
        
        vertexCount++;
    }

    /**
     * Add a quad (2 triangles, 6 vertices) to the batch.
     */
    private void addQuad(float x, float y, float width, float height,
                        Color color, float sd1, float sd2, float sd3, float sd4,
                        int shapeType, int textureId) {
        // Triangle 1: TL, TR, BL
        addVertex(x, y, 0, 0, color, sd1, sd2, sd3, sd4, width, height, shapeType, textureId);
        addVertex(x + width, y, 1, 0, color, sd1, sd2, sd3, sd4, width, height, shapeType, textureId);
        addVertex(x, y + height, 0, 1, color, sd1, sd2, sd3, sd4, width, height, shapeType, textureId);
        
        // Triangle 2: BL, TR, BR
        addVertex(x, y + height, 0, 1, color, sd1, sd2, sd3, sd4, width, height, shapeType, textureId);
        addVertex(x + width, y, 1, 0, color, sd1, sd2, sd3, sd4, width, height, shapeType, textureId);
        addVertex(x + width, y + height, 1, 1, color, sd1, sd2, sd3, sd4, width, height, shapeType, textureId);
    }

    /**
     * Add a quad with custom UV coordinates (for textured images).
     */
    private void addQuadWithUVs(float x, float y, float width, float height,
                               Color color, float srcX, float srcY, float srcWidth, float srcHeight,
                               float sd1, float sd2, float sd3, float sd4,
                               int shapeType, int textureId) {
        // Calculate UVs for the source rectangle
        float u0 = srcX;
        float v0 = srcY;
        float u1 = srcX + srcWidth;
        float v1 = srcY + srcHeight;
        
        // Triangle 1: TL, TR, BL
        addVertex(x, y, u0, v0, color, sd1, sd2, sd3, sd4, width, height, shapeType, textureId);
        addVertex(x + width, y, u1, v0, color, sd1, sd2, sd3, sd4, width, height, shapeType, textureId);
        addVertex(x, y + height, u0, v1, color, sd1, sd2, sd3, sd4, width, height, shapeType, textureId);
        
        // Triangle 2: BL, TR, BR
        addVertex(x, y + height, u0, v1, color, sd1, sd2, sd3, sd4, width, height, shapeType, textureId);
        addVertex(x + width, y, u1, v0, color, sd1, sd2, sd3, sd4, width, height, shapeType, textureId);
        addVertex(x + width, y + height, u1, v1, color, sd1, sd2, sd3, sd4, width, height, shapeType, textureId);
    }

    // ============================================================================
    // Public Drawing API
    // ============================================================================

    /**
     * Draw a filled rounded rectangle.
     */
    public void fillRoundedRect(float x, float y, float width, float height, float radius, Color color) {
        if (cullingEnabled && shouldCull(x, y, width, height)) {
            return;
        }
        checkFlush(6);
        
        // For SDF shapes, we need padding for anti-aliasing
        float padding = 2.0f;
        float quadX = x - padding;
        float quadY = y - padding;
        float quadWidth = width + padding * 2;
        float quadHeight = height + padding * 2;
        
        // ShapeData: (width, height, radius, strokeWidth)
        addQuad(quadX, quadY, quadWidth, quadHeight, color,
               width, height, radius, 0.0f, SHAPE_ROUNDED_RECT, -1);
    }

    /**
     * Draw a stroked rounded rectangle.
     */
    public void strokeRoundedRect(float x, float y, float width, float height, float radius, float strokeWidth, Color color) {
        if (cullingEnabled && shouldCull(x, y, width, height)) {
            return;
        }
        checkFlush(6);
        
        // Expand quad for stroke
        float padding = strokeWidth / 2 + 2;
        float quadX = x - padding;
        float quadY = y - padding;
        float quadWidth = width + padding * 2;
        float quadHeight = height + padding * 2;
        
        // ShapeData: (width, height, radius, strokeWidth)
        addQuad(quadX, quadY, quadWidth, quadHeight, color,
               width, height, radius, strokeWidth, SHAPE_ROUNDED_RECT, -1);
    }

    /**
     * Draw a filled circle.
     */
    public void fillCircle(float centerX, float centerY, float radius, Color color) {
        if (cullingEnabled && shouldCull(centerX - radius, centerY - radius, radius * 2, radius * 2)) {
            return;
        }
        checkFlush(6);
        
        // Expand quad for anti-aliasing
        float padding = 2.0f;
        float size = radius * 2 + padding * 2;
        float x = centerX - radius - padding;
        float y = centerY - radius - padding;
        
        // ShapeData: (radius, strokeWidth, unused, unused)
        addQuad(x, y, size, size, color,
               radius, 0.0f, 0.0f, 0.0f, SHAPE_CIRCLE, -1);
    }

    /**
     * Draw a stroked circle.
     */
    public void strokeCircle(float centerX, float centerY, float radius, float strokeWidth, Color color) {
        if (cullingEnabled && shouldCull(centerX - radius, centerY - radius, radius * 2, radius * 2)) {
            return;
        }
        checkFlush(6);
        
        float padding = strokeWidth / 2 + 2;
        float size = radius * 2 + padding * 2;
        float x = centerX - radius - padding;
        float y = centerY - radius - padding;
        
        // ShapeData: (radius, strokeWidth, unused, unused)
        addQuad(x, y, size, size, color,
               radius, strokeWidth, 0.0f, 0.0f, SHAPE_CIRCLE, -1);
    }

    /**
     * Draw a line.
     * Creates an oriented rectangle along the line direction.
     */
    public void strokeLine(float x1, float y1, float x2, float y2, float lineWidth, Color color) {
        // Calculate bounding box for culling
        if (cullingEnabled) {
            float minX = Math.min(x1, x2) - lineWidth / 2;
            float minY = Math.min(y1, y2) - lineWidth / 2;
            float maxX = Math.max(x1, x2) + lineWidth / 2;
            float maxY = Math.max(y1, y2) + lineWidth / 2;
            if (shouldCull(minX, minY, maxX - minX, maxY - minY)) {
                return;
            }
        }
        checkFlush(6);
        
        // Calculate line vector and length
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (length < 0.001f) {
            // Zero-length line, render as a point
            fillPoint(x1, y1, lineWidth, color);
            return;
        }
        
        // Normalize direction
        float ndx = dx / length;
        float ndy = dy / length;
        
        // Perpendicular vector (rotated 90 degrees)
        float perpX = -ndy;
        float perpY = ndx;
        
        // Half width offset
        float hw = lineWidth * 0.5f;
        
        // Calculate the four corners of the oriented rectangle
        // Start point offsets
        float sx1 = x1 + perpX * hw;
        float sy1 = y1 + perpY * hw;
        float sx2 = x1 - perpX * hw;
        float sy2 = y1 - perpY * hw;
        
        // End point offsets
        float ex1 = x2 + perpX * hw;
        float ey1 = y2 + perpY * hw;
        float ex2 = x2 - perpX * hw;
        float ey2 = y2 - perpY * hw;
        
        // ShapeData: (lineWidth, unused, unused, unused)
        // Add the oriented quad manually (not using addQuad helper)
        // The quad dimensions are: width=length, height=lineWidth
        // Triangle 1: sx1,sy1 -> ex1,ey1 -> sx2,sy2
        addVertex(sx1, sy1, 0, 0, color, lineWidth, 0, 0, 0, length, lineWidth, SHAPE_LINE, -1);
        addVertex(ex1, ey1, 1, 0, color, lineWidth, 0, 0, 0, length, lineWidth, SHAPE_LINE, -1);
        addVertex(sx2, sy2, 0, 1, color, lineWidth, 0, 0, 0, length, lineWidth, SHAPE_LINE, -1);
        
        // Triangle 2: sx2,sy2 -> ex1,ey1 -> ex2,ey2
        addVertex(sx2, sy2, 0, 1, color, lineWidth, 0, 0, 0, length, lineWidth, SHAPE_LINE, -1);
        addVertex(ex1, ey1, 1, 0, color, lineWidth, 0, 0, 0, length, lineWidth, SHAPE_LINE, -1);
        addVertex(ex2, ey2, 1, 1, color, lineWidth, 0, 0, 0, length, lineWidth, SHAPE_LINE, -1);
    }

    /**
     * Draw a point (small filled circle).
     */
    public void fillPoint(float x, float y, float size, Color color) {
        if (cullingEnabled && shouldCull(x - size / 2, y - size / 2, size, size)) {
            return;
        }
        checkFlush(6);
        
        float padding = 2.0f;
        float quadSize = size + padding * 2;
        float quadX = x - size / 2 - padding;
        float quadY = y - size / 2 - padding;
        
        // ShapeData: (size, unused, unused, unused)
        addQuad(quadX, quadY, quadSize, quadSize, color,
               size / 2, 0.0f, 0.0f, 0.0f, SHAPE_POINT, -1);
    }

    /**
     * Draw a filled triangle (raw geometry, no SDF).
     * This is used by the path API for polygon filling.
     */
    public void fillTriangle(float x1, float y1, float x2, float y2, float x3, float y3, Color color) {
        // Calculate bounding box for culling
        if (cullingEnabled) {
            float minX = Math.min(x1, Math.min(x2, x3));
            float minY = Math.min(y1, Math.min(y2, y3));
            float maxX = Math.max(x1, Math.max(x2, x3));
            float maxY = Math.max(y1, Math.max(y2, y3));
            if (shouldCull(minX, minY, maxX - minX, maxY - minY)) {
                return;
            }
        }
        checkFlush(3); // Need 3 vertices for a triangle
        
        // Add three vertices forming a triangle
        // For raw triangles, we don't need SDF, just flat color
        // Use dummy texture coords and shape data
        addVertex(x1, y1, 0, 0, color, 0, 0, 0, 0, 0, 0, SHAPE_TRIANGLE, -1);
        addVertex(x2, y2, 0, 0, color, 0, 0, 0, 0, 0, 0, SHAPE_TRIANGLE, -1);
        addVertex(x3, y3, 0, 0, color, 0, 0, 0, 0, 0, 0, SHAPE_TRIANGLE, -1);
    }

    /**
     * Draw a textured quad (image).
     * 
     * @param x              X position
     * @param y              Y position
     * @param width          Width
     * @param height         Height
     * @param tint           Tint color (Color.WHITE for no tint)
     * @param srcX           Source texture X (normalized 0-1)
     * @param srcY           Source texture Y (normalized 0-1)
     * @param srcWidth       Source texture width (normalized 0-1)
     * @param srcHeight      Source texture height (normalized 0-1)
     * @param textureSlot    Texture slot index (0-7)
     */
    public void drawTexturedQuad(float x, float y, float width, float height, Color tint,
                                float srcX, float srcY, float srcWidth, float srcHeight,
                                int textureSlot) {
        if (cullingEnabled && shouldCull(x, y, width, height)) {
            return;
        }
        checkFlush(6);
        
        // No padding needed for textured quads - they're not SDF-based
        // ShapeData is unused for textured quads, but we pass dummy values
        addQuadWithUVs(x, y, width, height, tint,
                      srcX, srcY, srcWidth, srcHeight,
                      0, 0, 0, 0, SHAPE_TEXTURED_QUAD, textureSlot);
    }

    /**
     * Draw text using SDF font.
     */
    public void drawText(String text, SdfFont font, float x, float y, Color color) {
        drawText(text, font, x, y, color, Color.BLACK, 0.0f, 0.0f, 0.0f);
    }
    
    /**
     * Draw text using SDF font with stroke support.
     * 
     * @param text The text to render
     * @param font The SDF font to use
     * @param x X position (top-left)
     * @param y Y position (top-left)
     * @param fillColor Text fill color
     * @param strokeColor Text stroke/outline color
     * @param strokeWidth Stroke width (0 = no stroke)
     * @param letterSpacing Additional spacing between letters
     * @param lineSpacing Additional vertical spacing between lines
     */
    public void drawText(String text, SdfFont font, float x, float y, Color fillColor, 
                        Color strokeColor, float strokeWidth, float letterSpacing, float lineSpacing) {
        if (text == null || text.isEmpty() || font == null) {
            return;
        }
        
        // Bind font atlas texture if different from current
        if (currentFont != font) {
            // For now, we'll flush when font changes
            // Future optimization: use texture arrays for multiple fonts
            flush();
            currentFont = font;
            
            // Bind the font atlas texture
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, ((org.pixel.content.opengl.GLTexture) font.getAtlasTexture()).getId());
            glUniform1i(shader.getUniformLocation("uTextAtlas"), 0);
        }
        
        // Render glyphs
        final float sdfPadding = 4.0f;
        float cursorX = x;
        float cursorY = y;
        
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            
            // Handle newlines
            if (ch == '\n') {
                cursorX = x;
                cursorY += font.getLineHeight() + lineSpacing;
                continue;
            }
            
            // Handle spaces
            if (ch == ' ') {
                float spaceWidth = font.getFontSize() * GLSdfConstants.SPACE_WIDTH_RATIO;
                cursorX += spaceWidth + letterSpacing;
                continue;
            }
            
            org.pixel.graphics.render.canvas.text.SdfGlyph glyph = font.getGlyph(ch);
            if (glyph == null) {
                continue; // Skip unknown characters
            }
            
            // Calculate glyph quad position
            float glyphX = cursorX + glyph.getOffsetX();
            float glyphY = cursorY + font.getAscent() + glyph.getOffsetY() - (sdfPadding / 2f); // - padding
            float glyphW = glyph.getWidth();
            float glyphH = glyph.getHeight();
            
            // Cull individual glyphs that are off-screen
            if (cullingEnabled && shouldCull(glyphX, glyphY, glyphW, glyphH)) {
                // Still advance cursor for spacing consistency
                cursorX += glyph.getAdvance() + letterSpacing;
                continue;
            }
            
            // Check if we need to flush before adding glyph quad
            checkFlush(6);
            
            // Calculate texture coordinates (normalized)
            float atlasW = font.getAtlasWidth();
            float atlasH = font.getAtlasHeight();
            float u0 = glyph.getAtlasX() / atlasW;
            float v0 = glyph.getAtlasY() / atlasH;
            float u1 = (glyph.getAtlasX() + glyph.getWidth()) / atlasW;
            float v1 = (glyph.getAtlasY() + glyph.getHeight()) / atlasH;
            
            // ShapeData: (strokeWidth, strokeR, strokeG, strokeB)
            // Normalize stroke width to SDF space
            float normalizedStrokeWidth = strokeWidth / (sdfPadding * 4.0f);
            
            // Pack stroke color into shapeData
            float strokeR = strokeColor.getRed();
            float strokeG = strokeColor.getGreen();
            float strokeB = strokeColor.getBlue();
            
            // Add glyph quad with proper texture coordinates
            // Quad size is just the glyph size for text (no padding/expansion needed for SDF text)
            // Triangle 1: TL, TR, BL
            addVertex(glyphX, glyphY, u0, v0, fillColor, 
                     normalizedStrokeWidth, strokeR, strokeG, strokeB, glyphW, glyphH, SHAPE_TEXT_GLYPH, 0);
            addVertex(glyphX + glyphW, glyphY, u1, v0, fillColor,
                     normalizedStrokeWidth, strokeR, strokeG, strokeB, glyphW, glyphH, SHAPE_TEXT_GLYPH, 0);
            addVertex(glyphX, glyphY + glyphH, u0, v1, fillColor,
                     normalizedStrokeWidth, strokeR, strokeG, strokeB, glyphW, glyphH, SHAPE_TEXT_GLYPH, 0);
            
            // Triangle 2: BL, TR, BR
            addVertex(glyphX, glyphY + glyphH, u0, v1, fillColor,
                     normalizedStrokeWidth, strokeR, strokeG, strokeB, glyphW, glyphH, SHAPE_TEXT_GLYPH, 0);
            addVertex(glyphX + glyphW, glyphY, u1, v0, fillColor,
                     normalizedStrokeWidth, strokeR, strokeG, strokeB, glyphW, glyphH, SHAPE_TEXT_GLYPH, 0);
            addVertex(glyphX + glyphW, glyphY + glyphH, u1, v1, fillColor,
                     normalizedStrokeWidth, strokeR, strokeG, strokeB, glyphW, glyphH, SHAPE_TEXT_GLYPH, 0);
            
            // Advance cursor
            cursorX += glyph.getAdvance() + letterSpacing;
        }
    }

    // ============================================================================
    // Gradient Support (Per-Vertex Colors)
    // ============================================================================

    /**
     * Draw a gradient-filled quad with per-vertex colors.
     * The GPU automatically interpolates colors between vertices.
     * Uses SHAPE_TRIANGLE type for raw triangle rendering without SDF.
     * 
     * @param x      X position
     * @param y      Y position
     * @param width  Width
     * @param height Height
     * @param c1     Top-left color
     * @param c2     Top-right color
     * @param c3     Bottom-right color
     * @param c4     Bottom-left color
     */
    public void fillRectGradient(float x, float y, float width, float height,
                                 Color c1, Color c2, Color c3, Color c4) {
        if (cullingEnabled && shouldCull(x, y, width, height)) {
            return;
        }
        checkFlush(6);
        
        // Use SHAPE_TRIANGLE type - no SDF, just raw triangles with interpolated colors
        // ShapeData is unused for triangles (all zeros)
        float[][] mat = currentLocalTransform.toUnsafeArray();
        
        // Transform vertices on CPU
        float x1 = mat[0][0] * x + mat[1][0] * y + mat[3][0];
        float y1 = mat[0][1] * x + mat[1][1] * y + mat[3][1];
        
        float x2 = mat[0][0] * (x + width) + mat[1][0] * y + mat[3][0];
        float y2 = mat[0][1] * (x + width) + mat[1][1] * y + mat[3][1];
        
        float x3 = mat[0][0] * (x + width) + mat[1][0] * (y + height) + mat[3][0];
        float y3 = mat[0][1] * (x + width) + mat[1][1] * (y + height) + mat[3][1];
        
        float x4 = mat[0][0] * x + mat[1][0] * (y + height) + mat[3][0];
        float y4 = mat[0][1] * x + mat[1][1] * (y + height) + mat[3][1];
        
        // Triangle 1: TL, TR, BR
        addVertexRaw(x1, y1, 0, 0, c1);
        addVertexRaw(x2, y2, 1, 0, c2);
        addVertexRaw(x3, y3, 1, 1, c3);
        
        // Triangle 2: TL, BR, BL
        addVertexRaw(x1, y1, 0, 0, c1);
        addVertexRaw(x3, y3, 1, 1, c3);
        addVertexRaw(x4, y4, 0, 1, c4);
    }

    /**
     * Draw a circle with radial gradient using a triangle fan.
     * Approximates radial gradient from center to edge.
     * 
     * @param centerX     Center X
     * @param centerY     Center Y
     * @param radius      Radius
     * @param centerColor Color at center
     * @param edgeColor   Color at edge
     */
    public void fillCircleRadialGradient(float centerX, float centerY, float radius,
                                         Color centerColor, Color edgeColor) {
        int segments = 32; // Number of edge segments for smoothness
        int verticesNeeded = segments * 3; // Each triangle uses 3 vertices
        
        if (cullingEnabled && shouldCull(centerX - radius, centerY - radius, radius * 2, radius * 2)) {
            return;
        }
        checkFlush(verticesNeeded);
        
        // Transform center point
        float[][] mat = currentLocalTransform.toUnsafeArray();
        float cx = mat[0][0] * centerX + mat[1][0] * centerY + mat[3][0];
        float cy = mat[0][1] * centerX + mat[1][1] * centerY + mat[3][1];
        
        // Extract scale for radius transformation
        float scaleX = (float) Math.sqrt(mat[0][0] * mat[0][0] + mat[0][1] * mat[0][1]);
        float scaleY = (float) Math.sqrt(mat[1][0] * mat[1][0] + mat[1][1] * mat[1][1]);
        float avgScale = (scaleX + scaleY) / 2.0f;
        float transformedRadius = radius * avgScale;
        
        // Draw triangle fan: center vertex to edge vertices
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (2 * Math.PI * i / segments);
            float angle2 = (float) (2 * Math.PI * (i + 1) / segments);
            
            float x1 = cx + transformedRadius * (float) Math.cos(angle1);
            float y1 = cy + transformedRadius * (float) Math.sin(angle1);
            
            float x2 = cx + transformedRadius * (float) Math.cos(angle2);
            float y2 = cy + transformedRadius * (float) Math.sin(angle2);
            
            // Triangle: center, edge1, edge2
            addVertexRaw(cx, cy, 0.5f, 0.5f, centerColor);
            addVertexRaw(x1, y1, 0, 0, edgeColor);
            addVertexRaw(x2, y2, 1, 1, edgeColor);
        }
    }

    /**
     * Add a raw vertex for gradient rendering (already transformed).
     * Used for SHAPE_TRIANGLE type which doesn't use SDF.
     */
    private void addVertexRaw(float x, float y, float u, float v, Color color) {
        // Position (already transformed)
        vertexBuffer.put(x);
        vertexBuffer.put(y);
        
        // Texture coords (unused for triangles but required)
        vertexBuffer.put(u);
        vertexBuffer.put(v);
        
        // Color (per-vertex!)
        vertexBuffer.put(color.getRed());
        vertexBuffer.put(color.getGreen());
        vertexBuffer.put(color.getBlue());
        vertexBuffer.put(color.getAlpha());
        
        // ShapeData (unused for raw triangles)
        vertexBuffer.put(0);
        vertexBuffer.put(0);
        vertexBuffer.put(0);
        vertexBuffer.put(0);
        
        // QuadSize (unused)
        vertexBuffer.put(0);
        vertexBuffer.put(0);
        
        // ShapeType (SHAPE_TRIANGLE)
        vertexBuffer.put((float) SHAPE_TRIANGLE);
        
        // TextureId (no texture)
        vertexBuffer.put(-1);
        
        vertexCount++;
    }

    // ============================================================================
    // Culling Support
    // ============================================================================

    /**
     * Enable or disable automatic viewport culling.
     * When enabled, primitives completely outside the viewport are automatically skipped.
     * 
     * @param enabled true to enable culling, false to disable
     */
    public void setCullingEnabled(boolean enabled) {
        this.cullingEnabled = enabled;
    }

    /**
     * Set custom culling bounds (overrides automatic viewport extraction).
     * 
     * @param minX Minimum X coordinate
     * @param minY Minimum Y coordinate
     * @param maxX Maximum X coordinate
     * @param maxY Maximum Y coordinate
     */
    public void setCullingBounds(float minX, float minY, float maxX, float maxY) {
        this.cullingMinX = minX;
        this.cullingMinY = minY;
        this.cullingMaxX = maxX;
        this.cullingMaxY = maxY;
    }

    /**
     * Extract viewport bounds from an orthographic projection matrix.
     * Assumes matrix is: ortho(left, right, bottom, top, near, far)
     */
    private void extractViewportBounds(Matrix4 viewMatrix) {
        float[][] m = viewMatrix.toUnsafeArray();
        
        // For orthographic projection: ortho(left, right, bottom, top, near, far)
        // Matrix form:
        //   [2/(r-l),    0,         0,        -(r+l)/(r-l)]
        //   [0,          2/(t-b),   0,        -(t+b)/(t-b)]
        //   [0,          0,         -2/(f-n), -(f+n)/(f-n)]
        //   [0,          0,         0,        1           ]
        
        // Extract left, right, bottom, top from matrix elements
        // left   = -(m[3][0] + 1) / m[0][0]
        // right  = -(m[3][0] - 1) / m[0][0]
        // bottom = -(m[3][1] + 1) / m[1][1]
        // top    = -(m[3][1] - 1) / m[1][1]
        
        if (m[0][0] != 0 && m[1][1] != 0) {
            float left   = -(m[3][0] + 1) / m[0][0];
            float right  = -(m[3][0] - 1) / m[0][0];
            float bottom = -(m[3][1] + 1) / m[1][1];
            float top    = -(m[3][1] - 1) / m[1][1];
            
            this.cullingMinX = left;
            this.cullingMinY = top;    // Note: Y is typically inverted in screen space
            this.cullingMaxX = right;
            this.cullingMaxY = bottom;
        } else {
            // Fallback: no culling
            this.cullingMinX = Float.NEGATIVE_INFINITY;
            this.cullingMinY = Float.NEGATIVE_INFINITY;
            this.cullingMaxX = Float.POSITIVE_INFINITY;
            this.cullingMaxY = Float.POSITIVE_INFINITY;
        }
    }

    /**
     * Check if a bounding box (in local space, before transform) should be culled.
     * Applies the current local transform to get world-space bounds, then tests against culling bounds.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width
     * @param height Height
     * @return true if the primitive should be culled (completely outside viewport)
     */
    private boolean shouldCull(float x, float y, float width, float height) {
        if (!cullingEnabled) {
            return false;
        }
        
        // Transform the 4 corners of the bounding box by the current local transform
        float[][] m = currentLocalTransform.toUnsafeArray();
        
        // Corner 1: (x, y)
        float x1 = m[0][0] * x + m[1][0] * y + m[3][0];
        float y1 = m[0][1] * x + m[1][1] * y + m[3][1];
        
        // Corner 2: (x + width, y)
        float x2 = m[0][0] * (x + width) + m[1][0] * y + m[3][0];
        float y2 = m[0][1] * (x + width) + m[1][1] * y + m[3][1];
        
        // Corner 3: (x, y + height)
        float x3 = m[0][0] * x + m[1][0] * (y + height) + m[3][0];
        float y3 = m[0][1] * x + m[1][1] * (y + height) + m[3][1];
        
        // Corner 4: (x + width, y + height)
        float x4 = m[0][0] * (x + width) + m[1][0] * (y + height) + m[3][0];
        float y4 = m[0][1] * (x + width) + m[1][1] * (y + height) + m[3][1];
        
        // Get AABB of transformed corners
        float minX = Math.min(Math.min(x1, x2), Math.min(x3, x4));
        float minY = Math.min(Math.min(y1, y2), Math.min(y3, y4));
        float maxX = Math.max(Math.max(x1, x2), Math.max(x3, x4));
        float maxY = Math.max(Math.max(y1, y2), Math.max(y3, y4));
        
        // Test against culling bounds (AABB intersection test)
        return maxX < cullingMinX || minX > cullingMaxX ||
               maxY < cullingMinY || minY > cullingMaxY;
    }

    /**
     * Dispose of resources.
     */
    public void dispose() {
        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(matrixBuffer);
        vao.dispose();
        vbo.dispose();
        shader.dispose();
    }
}
