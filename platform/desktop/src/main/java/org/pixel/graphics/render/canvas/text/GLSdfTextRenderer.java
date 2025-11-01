/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render.canvas.text;

import org.lwjgl.system.MemoryUtil;
import org.pixel.commons.Color;
import org.pixel.graphics.render.SdfTextRenderer;
import org.pixel.graphics.render.canvas.GLSdfConstants;
import org.pixel.graphics.render.canvas.TextStyle;
import org.pixel.graphics.shader.opengl.GLSdfTextShader;
import org.pixel.graphics.shader.opengl.GLVertexArrayObject;
import org.pixel.graphics.shader.opengl.GLVertexBufferObject;
import org.pixel.content.opengl.GLTexture;
import org.pixel.math.Matrix4;
import org.pixel.math.Vector2;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Renders SDF text using OpenGL.
 * Renders each glyph as a quad with SDF shader for crisp edges and stroke support.
 */
public class GLSdfTextRenderer implements SdfTextRenderer {

    private static final int VERTEX_SIZE = 4; // x, y, u, v
    private static final int VERTICES_PER_GLYPH = 6; // 2 triangles
    private static final int FLOATS_PER_GLYPH = VERTICES_PER_GLYPH * VERTEX_SIZE;

    private final GLSdfTextShader shader;
    private final GLVertexArrayObject vao;
    private final GLVertexBufferObject vbo;
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer matrixBuffer;

    /**
     * Constructor.
     */
    public GLSdfTextRenderer() {
        this.shader = new GLSdfTextShader();
        this.vao = new GLVertexArrayObject();
        this.vbo = new GLVertexBufferObject();
        this.vertexBuffer = MemoryUtil.memAllocFloat(FLOATS_PER_GLYPH * 100); // Buffer for 100 glyphs
        this.matrixBuffer = MemoryUtil.memAllocFloat(16);

        initializeBuffers();
    }

    private void initializeBuffers() {
        vao.bind();
        vbo.bind(GL_ARRAY_BUFFER);

        // Allocate VBO (dynamic draw since text changes frequently)
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
     * Render text with the given style.
     *
     * @param text       The text to render
     * @param font       The SDF font to use
     * @param x          X position (world space)
     * @param y          Y position (world space)
     * @param style      Text styling
     * @param viewMatrix Camera view-projection matrix
     * @param scale      Scale factor (x, y) for the text
     */
    @Override
    public void render(String text, SdfFont font, float x, float y, TextStyle style, Matrix4 viewMatrix, Vector2 scale) {
        if (text == null || text.isEmpty() || font == null) {
            return;
        }

        // Render drop shadow first (if enabled)
        if (style.isDropShadow()) {
            Vector2 shadowOffset = style.getShadowOffset();
            Color shadowColor = style.getShadowColor();

            // Create shadow style with same stroke width and letter spacing as main text
            // This ensures the shadow has identical spacing and length
            TextStyle shadowStyle = new TextStyle(shadowColor);
            shadowStyle.setStroke(null, style.getStrokeWidth()); // Copy stroke width for spacing
            shadowStyle.setLetterSpacing(style.getLetterSpacing()); // Copy letter spacing too!

            renderPass(text, font,
                x + shadowOffset.getX() * scale.getX(),
                y + shadowOffset.getY() * scale.getY(),
                shadowStyle, viewMatrix, scale);
        }

        // Render main text
        renderPass(text, font, x, y, style, viewMatrix, scale);
    }

    /**
     * Internal method to render a single text pass.
     */
    private void renderPass(String text, SdfFont font, float x, float y, TextStyle style, Matrix4 viewMatrix, Vector2 scale) {
        shader.bind();
        vao.bind();

        // Upload view matrix
        matrixBuffer.clear();
        viewMatrix.writeBuffer(matrixBuffer);
        glUniformMatrix4fv(shader.getUniformLocation("uViewMatrix"), false, matrixBuffer);

        // Bind atlas texture
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, ((GLTexture) font.getAtlasTexture()).getId());
        glUniform1i(shader.getUniformLocation("uAtlas"), 0);

        // Upload fill color
        Color fillColor = style.getFillColor() != null ? style.getFillColor() : Color.WHITE;
        glUniform4f(shader.getUniformLocation("uFillColor"),
            fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), fillColor.getAlpha());

        // Upload stroke color and width
        if (style.hasStroke()) {
            Color strokeColor = style.getStrokeColor();
            glUniform4f(shader.getUniformLocation("uStrokeColor"),
                strokeColor.getRed(), strokeColor.getGreen(), strokeColor.getBlue(), strokeColor.getAlpha());
            
            // Convert stroke width from pixels to SDF space
            // SDF padding is 4 pixels, which maps to the distance field range
            // Stroke width in shader is how far from edge (0.5) to extend
            // A reasonable mapping: strokePixels / (sdfPadding * 2) gives 0-1 range
            float sdfPadding = 4.0f;
            float normalizedStrokeWidth = style.getStrokeWidth() / (sdfPadding * 4.0f);
            glUniform1f(shader.getUniformLocation("uStrokeWidth"), normalizedStrokeWidth);
        } else {
            // No stroke - transparent stroke color
            glUniform4f(shader.getUniformLocation("uStrokeColor"), 0, 0, 0, 0);
            glUniform1f(shader.getUniformLocation("uStrokeWidth"), 0);
        }

        // Smoothness for anti-aliasing with SDF
        // This value controls the edge softness
        glUniform1f(shader.getUniformLocation("uSmoothness"), 0.075f);
        
        // Set text edge threshold for controlling text weight/boldness
        glUniform1f(shader.getUniformLocation("uTextEdge"), 
            GLSdfConstants.SDF_TEXT_EDGE_THRESHOLD);

        // Set up blending for text rendering
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Render glyphs
        float cursorX = x;
        float cursorY = y;
        int glyphCount = 0;

        vertexBuffer.clear();

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            // Handle newlines
            if (ch == '\n') {
                cursorX = x;
                cursorY += font.getLineHeight() * scale.getY();
                continue;
            }

            // Handle spaces - advance cursor even though there's no glyph to render
            if (ch == ' ') {
                // Use a standard space width (typically fontSize / 4, but we'll use a reasonable default)
                float spaceWidth = font.getFontSize() * GLSdfConstants.SPACE_WIDTH_RATIO * scale.getX();
                // Note: strokeWidth affects visual appearance but NOT glyph spacing
                cursorX += spaceWidth + (style.getLetterSpacing() * scale.getX());
                continue;
            }

            SdfGlyph glyph = font.getGlyph(ch);
            if (glyph == null) {
                continue; // Skip unknown characters
            }

            // Calculate glyph quad position with scale applied
            // cursorY = top of text line (UI-style positioning)
            // baseline = cursorY + ascent (ascent pixels down from top, since Y increases downward)
            // glyphY = baseline + offsetY (offsetY is negative, placing glyph above baseline)
            // Combined: glyphY = cursorY + ascent + offsetY
            float glyphX = cursorX + (glyph.getOffsetX() * scale.getX());
            float glyphY = cursorY + (font.getAscent() * scale.getY()) + (glyph.getOffsetY() * scale.getY());
            float glyphW = glyph.getWidth() * scale.getX();
            float glyphH = glyph.getHeight() * scale.getY();

            // Calculate texture coordinates (normalized)
            float atlasW = font.getAtlasWidth();
            float atlasH = font.getAtlasHeight();
            float u0 = glyph.getAtlasX() / atlasW;
            float v0 = glyph.getAtlasY() / atlasH;
            float u1 = (glyph.getAtlasX() + glyph.getWidth()) / atlasW;
            float v1 = (glyph.getAtlasY() + glyph.getHeight()) / atlasH;

            // Add quad vertices (2 triangles)
            // Triangle 1: TL, TR, BL
            addVertex(glyphX, glyphY, u0, v0);
            addVertex(glyphX + glyphW, glyphY, u1, v0);
            addVertex(glyphX, glyphY + glyphH, u0, v1);

            // Triangle 2: BL, TR, BR
            addVertex(glyphX, glyphY + glyphH, u0, v1);
            addVertex(glyphX + glyphW, glyphY, u1, v0);
            addVertex(glyphX + glyphW, glyphY + glyphH, u1, v1);

            glyphCount++;

            // Advance cursor with scale applied
            // Note: strokeWidth affects visual appearance but NOT glyph spacing
            cursorX += (glyph.getAdvance() * scale.getX()) + (style.getLetterSpacing() * scale.getX());

            // Flush if buffer is full
            if (glyphCount >= 100) {
                flushBatch(glyphCount);
                glyphCount = 0;
                vertexBuffer.clear();
            }
        }

        // Flush remaining glyphs
        if (glyphCount > 0) {
            flushBatch(glyphCount);
        }

        // Restore blending state (leave it disabled for next renderer)
        glDisable(GL_BLEND);
        vao.unbind();
    }

    private void addVertex(float x, float y, float u, float v) {
        vertexBuffer.put(x);
        vertexBuffer.put(y);
        vertexBuffer.put(u);
        vertexBuffer.put(v);
    }

    private void flushBatch(int glyphCount) {
        vertexBuffer.flip();

        vbo.bind(GL_ARRAY_BUFFER);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);

        glDrawArrays(GL_TRIANGLES, 0, glyphCount * VERTICES_PER_GLYPH);

        vertexBuffer.clear();
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
