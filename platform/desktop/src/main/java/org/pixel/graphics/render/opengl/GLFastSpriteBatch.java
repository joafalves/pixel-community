package org.pixel.graphics.render.opengl;

import org.lwjgl.system.MemoryUtil;
import org.pixel.commons.Color;
import org.pixel.commons.lifecycle.State;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.Font;
import org.pixel.content.FontGlyph;
import org.pixel.content.Texture;
import org.pixel.content.opengl.GLFont;
import org.pixel.content.opengl.GLTexture;
import org.pixel.graphics.render.BlendMode;
import org.pixel.graphics.render.SpriteBatch;
import org.pixel.graphics.shader.Shader;
import org.pixel.graphics.shader.opengl.GLInstancedMultiTextureShader;
import org.pixel.graphics.shader.opengl.GLShader;
import org.pixel.graphics.shader.opengl.GLVertexArrayObject;
import org.pixel.graphics.shader.opengl.GLVertexBufferObject;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.nio.FloatBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.*;
import static org.lwjgl.opengl.GL31C.glDrawArraysInstanced;
import static org.lwjgl.opengl.GL33C.glVertexAttribDivisor;

public class GLFastSpriteBatch extends SpriteBatch {

    private static final Logger log = LoggerFactory.getLogger(GLFastSpriteBatch.class);

    // Each instance needs position, size, anchor, rotation, color, source, and texIndex
    private static final int INSTANCE_FLOAT_COUNT = 2 + 2 + 2 + 1 + 4 + 4 + 1; // 16 floats per instance
    private static final int INSTANCE_BYTE_COUNT = INSTANCE_FLOAT_COUNT * Float.BYTES;
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private final GLVertexArrayObject vao;
    private final GLVertexBufferObject quadVbo; // VBO for the quad vertices
    private final GLVertexBufferObject instanceVbo; // VBO for the instance data
    private final FloatBuffer matrixBuffer;
    private final int shaderTextureCount;

    private State state = State.NEW;
    private GLShader defaultShader;      // The default instanced multi-texture shader
    private GLShader currentShader;      // The currently active shader (default or custom)
    private Matrix4 currentViewMatrix;   // Cached view matrix for shader switching
    private int bufferMaxSize;
    private int bufferWriteIndex;
    private FloatBuffer instanceDataBuffer;

    //region Struct of Arrays (SoA) for sprite data
    private float[] positionX, positionY;
    private float[] sizeX, sizeY;
    private float[] anchorX, anchorY;
    private float[] rotation;
    private float[] colorR, colorG, colorB, colorA;
    private float[] sourceX, sourceY, sourceWidth, sourceHeight;
    private float[] textureIndex;
    private int[] textureId;
    //endregion

    public GLFastSpriteBatch() {
        this(DEFAULT_BUFFER_SIZE); // Instancing is efficient, use a larger default buffer
    }

    public GLFastSpriteBatch(int bufferMaxSize) {
        this(bufferMaxSize, 0);
    }

    public GLFastSpriteBatch(int bufferMaxSize, int shaderTextureCount) {
        if (bufferMaxSize <= 0) {
            throw new RuntimeException("Invalid buffer size");
        }
        this.bufferMaxSize = bufferMaxSize;
        this.matrixBuffer = MemoryUtil.memAllocFloat(16);
        this.vao = new GLVertexArrayObject();
        this.quadVbo = new GLVertexBufferObject();
        this.instanceVbo = new GLVertexBufferObject();

        if (shaderTextureCount <= 0) {
            int[] textureUnits = new int[1];
            glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, textureUnits);
            this.shaderTextureCount = Math.max(textureUnits[0], 1);
        } else {
            this.shaderTextureCount = shaderTextureCount;
        }
        log.trace("FastSpriteBatch buffer max size: {}.", this.bufferMaxSize);
        log.trace("FastSpriteBatch shader texture count: {}.", this.shaderTextureCount);
    }

    @Override
    public boolean init() {
        if (state.hasInitialized()) return false;
        state = State.INITIALIZING;

        defaultShader = new GLInstancedMultiTextureShader(shaderTextureCount);
        currentShader = defaultShader;
        defaultShader.bind();

        int[] textureRefArray = new int[shaderTextureCount];
        for (int i = 0; i < shaderTextureCount; i++) textureRefArray[i] = i;
        glUniform1iv(defaultShader.getUniformLocation("uTextureImage"), textureRefArray);

        vao.bind();

        // 1. Quad VBO (static)
        FloatBuffer quadBuffer = null;
        try {
            float[] quadVertices = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f};
            quadBuffer = MemoryUtil.memAllocFloat(quadVertices.length);
            quadBuffer.put(quadVertices).flip();

            quadVbo.bind(GL_ARRAY_BUFFER);
            quadVbo.uploadData(GL_ARRAY_BUFFER, quadBuffer, GL_STATIC_DRAW);

        } finally {
            if (quadBuffer != null) {
                MemoryUtil.memFree(quadBuffer);
            }
        }
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

        // 2. Instance VBO (dynamic)
        instanceVbo.bind(GL_ARRAY_BUFFER);
        instanceVbo.uploadData(GL_ARRAY_BUFFER, (long) bufferMaxSize * INSTANCE_BYTE_COUNT, GL_DYNAMIC_DRAW);

        // Define instance attributes
        int offset = 0;
        glEnableVertexAttribArray(1); // iPosition
        glVertexAttribPointer(1, 2, GL_FLOAT, false, INSTANCE_BYTE_COUNT, offset);
        glVertexAttribDivisor(1, 1);
        offset += 2 * Float.BYTES;

        glEnableVertexAttribArray(2); // iSize
        glVertexAttribPointer(2, 2, GL_FLOAT, false, INSTANCE_BYTE_COUNT, offset);
        glVertexAttribDivisor(2, 1);
        offset += 2 * Float.BYTES;

        glEnableVertexAttribArray(3); // iAnchor
        glVertexAttribPointer(3, 2, GL_FLOAT, false, INSTANCE_BYTE_COUNT, offset);
        glVertexAttribDivisor(3, 1);
        offset += 2 * Float.BYTES;

        glEnableVertexAttribArray(4); // iRotation
        glVertexAttribPointer(4, 1, GL_FLOAT, false, INSTANCE_BYTE_COUNT, offset);
        glVertexAttribDivisor(4, 1);
        offset += 1 * Float.BYTES;

        glEnableVertexAttribArray(5); // iColor
        glVertexAttribPointer(5, 4, GL_FLOAT, false, INSTANCE_BYTE_COUNT, offset);
        glVertexAttribDivisor(5, 1);
        offset += 4 * Float.BYTES;

        glEnableVertexAttribArray(6); // iSource
        glVertexAttribPointer(6, 4, GL_FLOAT, false, INSTANCE_BYTE_COUNT, offset);
        glVertexAttribDivisor(6, 1);
        offset += 4 * Float.BYTES;

        glEnableVertexAttribArray(7); // iTexIndex
        glVertexAttribPointer(7, 1, GL_FLOAT, false, INSTANCE_BYTE_COUNT, offset);
        glVertexAttribDivisor(7, 1);

        vao.unbind();
        this.initBuffer();
        state = State.INITIALIZED;
        return true;
    }

    @Override
    public void dispose() {
        defaultShader.dispose();
        quadVbo.dispose();
        instanceVbo.dispose();
        vao.dispose();
        MemoryUtil.memFree(matrixBuffer);
        if (instanceDataBuffer != null) MemoryUtil.memFree(instanceDataBuffer);
        state = State.DISPOSED;
    }

    @Override
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scaleX,
                     float scaleY, float rotation, int depth) {
        draw(texture, position, source, color, anchor, scaleX, scaleY, rotation, depth, null, null);
    }
    
    @Override
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scaleX,
                     float scaleY, float rotation, int depth, Shader customShader, org.pixel.commons.data.DataMap uniforms) {
        // Check if we need to switch shaders
        GLShader targetShader = (customShader != null) ? (GLShader) customShader : defaultShader;
        
        if (targetShader != currentShader) {
            // Switching to a different shader - flush and switch
            switchShader(targetShader);
        }
        
        // When using a custom shader, we need to flush immediately before AND after each sprite
        // because custom shaders with different uniform values cannot be batched together
        if (customShader != null) {
            // Flush any pending sprites first
            if (bufferWriteIndex > 0) flush();
            
            // Apply uniforms for this sprite
            if (uniforms != null && !uniforms.isEmpty()) {
                applyUniforms(uniforms);
            }
        }
        
        if (bufferWriteIndex >= bufferMaxSize) flush();

        this.positionX[bufferWriteIndex] = position.getX();
        this.positionY[bufferWriteIndex] = position.getY();
        // Apply scale to source dimensions if source is provided, otherwise use full texture
        this.sizeX[bufferWriteIndex] = texture.getWidth() * (source != null ? source.getWidth() / texture.getWidth() * scaleX : scaleX);
        this.sizeY[bufferWriteIndex] = texture.getHeight() * (source != null ? source.getHeight() / texture.getHeight() * scaleY : scaleY);
        this.anchorX[bufferWriteIndex] = anchor.getX();
        this.anchorY[bufferWriteIndex] = anchor.getY();
        this.rotation[bufferWriteIndex] = rotation;
        this.colorR[bufferWriteIndex] = color.getRed();
        this.colorG[bufferWriteIndex] = color.getGreen();
        this.colorB[bufferWriteIndex] = color.getBlue();
        this.colorA[bufferWriteIndex] = color.getAlpha();
        this.textureId[bufferWriteIndex] = ((GLTexture) texture).getId();

        if (source != null) {
            float texW = texture.getWidth();
            float texH = texture.getHeight();
            this.sourceX[bufferWriteIndex] = source.getX() / texW;
            this.sourceY[bufferWriteIndex] = source.getY() / texH;
            this.sourceWidth[bufferWriteIndex] = source.getWidth() / texW;
            this.sourceHeight[bufferWriteIndex] = source.getHeight() / texH;
        } else {
            this.sourceX[bufferWriteIndex] = 0;
            this.sourceY[bufferWriteIndex] = 0;
            this.sourceWidth[bufferWriteIndex] = 1;
            this.sourceHeight[bufferWriteIndex] = 1;
        }

        bufferWriteIndex++;
        
        // For custom shaders, flush immediately after adding this sprite
        // This ensures uniforms are applied to exactly this sprite
        if (customShader != null) {
            flush();
        }
    }

    @Override
    public void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color, Vector2 anchor,
                     float rotation, int depth) {
        if (bufferWriteIndex >= bufferMaxSize) flush();

        this.positionX[bufferWriteIndex] = displayArea.getX();
        this.positionY[bufferWriteIndex] = displayArea.getY();
        this.sizeX[bufferWriteIndex] = displayArea.getWidth();
        this.sizeY[bufferWriteIndex] = displayArea.getHeight();
        this.anchorX[bufferWriteIndex] = anchor.getX();
        this.anchorY[bufferWriteIndex] = anchor.getY();
        this.rotation[bufferWriteIndex] = rotation;
        this.colorR[bufferWriteIndex] = color.getRed();
        this.colorG[bufferWriteIndex] = color.getGreen();
        this.colorB[bufferWriteIndex] = color.getBlue();
        this.colorA[bufferWriteIndex] = color.getAlpha();
        this.textureId[bufferWriteIndex] = ((GLTexture) texture).getId();

        if (source != null) {
            float texW = texture.getWidth();
            float texH = texture.getHeight();
            this.sourceX[bufferWriteIndex] = source.getX() / texW;
            this.sourceY[bufferWriteIndex] = source.getY() / texH;
            this.sourceWidth[bufferWriteIndex] = source.getWidth() / texW;
            this.sourceHeight[bufferWriteIndex] = source.getHeight() / texH;
        } else {
            this.sourceX[bufferWriteIndex] = 0;
            this.sourceY[bufferWriteIndex] = 0;
            this.sourceWidth[bufferWriteIndex] = 1;
            this.sourceHeight[bufferWriteIndex] = 1;
        }

        bufferWriteIndex++;
    }

    @Override
    public void drawText(Font font, String text, Vector2 position, Color color, int fontSize) {
        float computedScale = fontSize / (float) font.getFontSize();
        float currentX = position.getX();
        float currentY = position.getY() + font.getFontSize() * computedScale + font.getVerticalSpacing();
        int fontTexId = ((GLFont) font).getTextureId();
        float fontTexSize = ((GLFont) font).getTextureSize();

        for (char ch : text.toCharArray()) {
            if (bufferWriteIndex >= bufferMaxSize) flush();

            FontGlyph glyph = font.getGlyph(ch);
            if (glyph == null) continue;

            if (ch == '\n') {
                currentY += font.getFontSize() * computedScale + font.getVerticalSpacing();
                currentX = position.getX();
                continue;
            }

            this.positionX[bufferWriteIndex] = currentX + glyph.getXOffset() * computedScale;
            this.positionY[bufferWriteIndex] = currentY + glyph.getYOffset() * computedScale;
            this.sizeX[bufferWriteIndex] = glyph.getWidth() * computedScale;
            this.sizeY[bufferWriteIndex] = glyph.getHeight() * computedScale;
            this.anchorX[bufferWriteIndex] = 0;
            this.anchorY[bufferWriteIndex] = 0;
            this.rotation[bufferWriteIndex] = 0;
            this.colorR[bufferWriteIndex] = color.getRed();
            this.colorG[bufferWriteIndex] = color.getGreen();
            this.colorB[bufferWriteIndex] = color.getBlue();
            this.colorA[bufferWriteIndex] = color.getAlpha();
            this.textureId[bufferWriteIndex] = fontTexId;
            this.sourceX[bufferWriteIndex] = glyph.getX() / fontTexSize;
            this.sourceY[bufferWriteIndex] = glyph.getY() / fontTexSize;
            this.sourceWidth[bufferWriteIndex] = glyph.getWidth() / fontTexSize;
            this.sourceHeight[bufferWriteIndex] = glyph.getHeight() / fontTexSize;

            currentX += glyph.getXAdvance() * computedScale + font.getHorizontalSpacing();
            bufferWriteIndex++;
        }
    }

    @Override
    public Shader getShader() {
        return currentShader;
    }
    
    /**
     * Switch to a different shader, flushing the current batch.
     */
    private void switchShader(GLShader newShader) {
        if (newShader == currentShader) return;
        
        // Flush current batch before switching
        flush();
        
        // Switch shader
        currentShader = newShader;
        currentShader.bind();
        
        // Re-apply view matrix to new shader
        if (currentViewMatrix != null) {
            matrixBuffer.clear();
            currentViewMatrix.writeBuffer(matrixBuffer);
            glUniformMatrix4fv(currentShader.getUniformLocation("uMatrix"), false, matrixBuffer);
        }
        
        // Set up texture uniforms based on shader type
        if (currentShader == defaultShader) {
            // Default instanced shader: multi-texture array
            int[] textureRefArray = new int[shaderTextureCount];
            for (int i = 0; i < shaderTextureCount; i++) textureRefArray[i] = i;
            glUniform1iv(defaultShader.getUniformLocation("uTextureImage"), textureRefArray);
        } else {
            // Custom shader: single texture on unit 0
            currentShader.setUniform("uTextureImage", 0);
        }
    }
    
    /**
     * Apply custom shader uniforms.
     */
    private void applyUniforms(org.pixel.commons.data.DataMap uniforms) {
        if (uniforms == null || uniforms.isEmpty()) return;
        
        for (String key : uniforms.keySet()) {
            Object value = uniforms.get(key);
            if (value instanceof Float) {
                currentShader.setUniform(key, (Float) value);
            } else if (value instanceof Integer) {
                currentShader.setUniform(key, (Integer) value);
            } else if (value instanceof org.pixel.math.Vector2) {
                org.pixel.math.Vector2 v = (org.pixel.math.Vector2) value;
                currentShader.setUniform(key, v.getX(), v.getY());
            } else if (value instanceof org.pixel.math.Vector3) {
                org.pixel.math.Vector3 v = (org.pixel.math.Vector3) value;
                currentShader.setUniform(key, v.getX(), v.getY(), v.getZ());
            } else if (value instanceof Color) {
                Color c = (Color) value;
                currentShader.setUniform(key, c.getRed(), c.getGreen(), c.getBlue());
            }
            // Note: Matrix4 would need glUniformMatrix4fv - add if needed
        }
    }

    @Override
    public void begin(Matrix4 viewMatrix) {
        this.begin(viewMatrix, BlendMode.NORMAL_BLEND);
    }

    @Override
    public void begin(Matrix4 viewMatrix, BlendMode blendMode) {
        bufferWriteIndex = 0;
        currentViewMatrix = viewMatrix;
        currentShader = defaultShader; // Always start with default shader

        // Ensure blending is enabled for sprite rendering
        glEnable(GL_BLEND);
        
        if (blendMode == BlendMode.ADDITIVE) glBlendFunc(GL_ONE, GL_ONE);
        else if (blendMode == BlendMode.MULTIPLY) glBlendFunc(GL_DST_COLOR, GL_ZERO);
        else glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        currentShader.bind();
        vao.bind();

        matrixBuffer.clear();
        viewMatrix.writeBuffer(matrixBuffer);
        glUniformMatrix4fv(currentShader.getUniformLocation("uMatrix"), false, matrixBuffer);
    }

    @Override
    public void end() {
        flush();
        vao.unbind();
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void resizeBuffer(int newSize) {
        if (newSize <= 0 || newSize == bufferMaxSize) return;
        this.bufferMaxSize = newSize;
        this.initBuffer();
    }

    private void initBuffer() {
        if (this.instanceDataBuffer != null) MemoryUtil.memFree(instanceDataBuffer);
        this.instanceDataBuffer = MemoryUtil.memAllocFloat(bufferMaxSize * INSTANCE_FLOAT_COUNT);

        positionX = new float[bufferMaxSize];
        positionY = new float[bufferMaxSize];
        sizeX = new float[bufferMaxSize];
        sizeY = new float[bufferMaxSize];
        anchorX = new float[bufferMaxSize];
        anchorY = new float[bufferMaxSize];
        rotation = new float[bufferMaxSize];
        colorR = new float[bufferMaxSize];
        colorG = new float[bufferMaxSize];
        colorB = new float[bufferMaxSize];
        colorA = new float[bufferMaxSize];
        sourceX = new float[bufferMaxSize];
        sourceY = new float[bufferMaxSize];
        sourceWidth = new float[bufferMaxSize];
        sourceHeight = new float[bufferMaxSize];
        textureIndex = new float[bufferMaxSize];
        textureId = new int[bufferMaxSize];
    }

    private void flush() {
        if (bufferWriteIndex == 0) return;

        HashMap<Integer, Integer> texUnitMap = new HashMap<>();
        int texUnitCounter = 0;
        int batchStart = 0;

        // This loop assigns texture units and determines batches
        for (int i = 0; i < bufferWriteIndex; i++) {
            int texID = this.textureId[i];
            if (!texUnitMap.containsKey(texID)) {
                if (texUnitCounter >= shaderTextureCount) {
                    renderBatch(batchStart, i, texUnitMap);
                    batchStart = i;
                    texUnitCounter = 0;
                    texUnitMap.clear();
                }
                texUnitMap.put(texID, texUnitCounter++);
            }
            this.textureIndex[i] = texUnitMap.get(texID);
        }
        // Render the final batch
        renderBatch(batchStart, bufferWriteIndex, texUnitMap);

        bufferWriteIndex = 0;
    }

    private void renderBatch(int start, int end, HashMap<Integer, Integer> texUnitMap) {
        int count = end - start;
        if (count <= 0) return;

        // Bind textures
        for (var entry : texUnitMap.entrySet()) {
            glActiveTexture(GL_TEXTURE0 + entry.getValue());
            glBindTexture(GL_TEXTURE_2D, entry.getKey());
        }

        // Prepare instance data buffer
        instanceDataBuffer.clear();
        for (int i = start; i < end; i++) {
            instanceDataBuffer.put(positionX[i]);
            instanceDataBuffer.put(positionY[i]);
            instanceDataBuffer.put(sizeX[i]);
            instanceDataBuffer.put(sizeY[i]);
            instanceDataBuffer.put(anchorX[i]);
            instanceDataBuffer.put(anchorY[i]);
            instanceDataBuffer.put(rotation[i]);
            instanceDataBuffer.put(colorR[i]);
            instanceDataBuffer.put(colorG[i]);
            instanceDataBuffer.put(colorB[i]);
            instanceDataBuffer.put(colorA[i]);
            instanceDataBuffer.put(sourceX[i]);
            instanceDataBuffer.put(sourceY[i]);
            instanceDataBuffer.put(sourceWidth[i]);
            instanceDataBuffer.put(sourceHeight[i]);
            instanceDataBuffer.put(textureIndex[i]);
        }
        instanceDataBuffer.flip();

        // Upload instance data and draw
        instanceVbo.bind(GL_ARRAY_BUFFER);
        glBufferSubData(GL_ARRAY_BUFFER, 0, instanceDataBuffer);
        glDrawArraysInstanced(GL_TRIANGLES, 0, 6, count);
    }
}
