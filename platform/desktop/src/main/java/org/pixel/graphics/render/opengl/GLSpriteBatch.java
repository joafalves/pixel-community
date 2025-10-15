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
import org.pixel.graphics.shader.opengl.GLMultiTextureShader;
import org.pixel.graphics.shader.opengl.GLShader;
import org.pixel.graphics.shader.opengl.GLVertexArrayObject;
import org.pixel.graphics.shader.opengl.GLVertexBufferObject;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL20C.*;

public class GLSpriteBatch extends SpriteBatch {

    private static final Logger log = LoggerFactory.getLogger(GLSpriteBatch.class);
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    private static final int SPRITE_UNIT_LENGTH = 54; // number of attribute information units per sprite
    private static final int ATTRIBUTE_STRIDE = 36; // attribute stride (bytes) between each vertex info

    private static final Matrix4 spriteViewMatrix = new Matrix4();
    private final Vector2 tTopLeft = new Vector2();
    private final Vector2 tTopRight = new Vector2();
    private final Vector2 tBottomRight = new Vector2();
    private final Vector2 tBottomLeft = new Vector2();
    private final Vector2 bottomLeft = new Vector2();
    private final Vector2 bottomRight = new Vector2();
    private final Vector2 topLeft = new Vector2();
    private final Vector2 topRight = new Vector2();
    private final HashMap<Integer, Integer> shaderTextureMap = new HashMap<>();
    private final GLVertexBufferObject vbo;
    private final GLVertexArrayObject vao;
    private final FloatBuffer matrixBuffer;
    private final int shaderTextureCount;

    private State state = State.NEW;
    private GLShader shader;
    private int bufferMaxSize;
    private int bufferWriteIndex;
    private boolean hasDifferentDepthLevels;
    private FloatBuffer dataBuffer;

    //region Struct of Arrays (SoA) for sprite data
    private int[] textureId;
    private int[] depth;
    private int[] textureWidth;
    private int[] textureHeight;
    private float[] rotation;
    private float[] x;
    private float[] y;
    private float[] width;
    private float[] height;
    private float[] colorR;
    private float[] colorG;
    private float[] colorB;
    private float[] colorA;
    private float[] anchorX;
    private float[] anchorY;
    private boolean[] hasSource;
    private float[] sourceX;
    private float[] sourceY;
    private float[] sourceWidth;
    private float[] sourceHeight;
    private Integer[] depthSortIndices;
    //endregion

    /**
     * Constructor.
     */
    public GLSpriteBatch() {
        this(DEFAULT_BUFFER_SIZE); // Default to a larger buffer size
    }

    /**
     * Constructor.
     *
     * @param bufferMaxSize The maximum number of sprites that can be drawn in a single batch.
     */
    public GLSpriteBatch(int bufferMaxSize) {
        this(bufferMaxSize, 0);
    }

    /**
     * Constructor.
     *
     * @param bufferMaxSize      The maximum number of sprites that can be drawn in a single batch.
     * @param shaderTextureCount The number of textures to be used by the shader.
     */
    public GLSpriteBatch(int bufferMaxSize, int shaderTextureCount) {
        if (bufferMaxSize <= 0) {
            throw new RuntimeException("Invalid buffer size, must be greater than zero");
        }

        this.bufferMaxSize = bufferMaxSize;
        this.matrixBuffer = MemoryUtil.memAllocFloat(16);
        this.vbo = new GLVertexBufferObject();
        this.vao = new GLVertexArrayObject();
        this.bufferWriteIndex = 0;

        if (shaderTextureCount <= 0) {
            int[] textureUnits = new int[1];
            glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, textureUnits);
            this.shaderTextureCount = Math.max(textureUnits[0], 1);
        } else {
            this.shaderTextureCount = shaderTextureCount;
        }

        log.trace("Buffer max size (units): {}.", this.bufferMaxSize);
        log.trace("Shader texture count: {}.", this.shaderTextureCount);
    }

    @Override
    public boolean init() {
        if (state.hasInitialized()) {
            log.warn("SpriteBatch already initialized.");
            return false;
        }
        state = State.INITIALIZING;

        shader = new GLMultiTextureShader(shaderTextureCount);
        shader.bind();

        int[] textureRefArray = new int[shaderTextureCount];
        for (int i = 0; i < shaderTextureCount; i++) {
            textureRefArray[i] = i;
        }
        glUniform1iv(shader.getUniformLocation("uTextureImage"), textureRefArray);

        int aVertexPosition = this.shader.getAttributeLocation("aVertexPosition");
        int aTextureCoordinates = this.shader.getAttributeLocation("aTextureCoordinates");
        int aVertexColor = this.shader.getAttributeLocation("aVertexColor");
        int aTextureId = this.shader.getAttributeLocation("aTextureIndex");

        vao.bind();
        vbo.bind(GL_ARRAY_BUFFER);

        glEnableVertexAttribArray(aVertexPosition);
        glVertexAttribPointer(aVertexPosition, 2, GL_FLOAT, false, ATTRIBUTE_STRIDE, 0);

        glEnableVertexAttribArray(aTextureCoordinates);
        glVertexAttribPointer(aTextureCoordinates, 2, GL_FLOAT, false, ATTRIBUTE_STRIDE, 2 * Float.BYTES);

        glEnableVertexAttribArray(aVertexColor);
        glVertexAttribPointer(aVertexColor, 4, GL_FLOAT, false, ATTRIBUTE_STRIDE, 4 * Float.BYTES);

        glEnableVertexAttribArray(aTextureId);
        glVertexAttribPointer(aTextureId, 1, GL_FLOAT, false, ATTRIBUTE_STRIDE, 8 * Float.BYTES);

        this.initBuffer();

        state = State.INITIALIZED;
        return true;
    }

    @Override
    public void dispose() {
        shader.dispose();
        vbo.dispose();
        vao.dispose();
        MemoryUtil.memFree(matrixBuffer);
        if (this.dataBuffer != null) {
            MemoryUtil.memFree(dataBuffer);
        }
        state = State.DISPOSED;
    }

    @Override
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scaleX,
                     float scaleY, float rotation, int depth) {
        // Delegate to new method with no custom shader
        draw(texture, position, source, color, anchor, scaleX, scaleY, rotation, depth, null, null);
    }
    
    @Override
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scaleX,
                     float scaleY, float rotation, int depth, Shader customShader, org.pixel.commons.data.DataMap uniforms) {
        // TODO: GLSpriteBatch doesn't support custom shaders yet - ignored
        // Use GLFastSpriteBatch for custom shader support
        if (bufferWriteIndex >= bufferMaxSize) {
            flush();
        }

        if (depth != 0) {
            hasDifferentDepthLevels = true;
        }

        this.textureId[bufferWriteIndex] = ((GLTexture) texture).getId();
        this.textureWidth[bufferWriteIndex] = texture.getWidth();
        this.textureHeight[bufferWriteIndex] = texture.getHeight();
        this.x[bufferWriteIndex] = position.getX();
        this.y[bufferWriteIndex] = position.getY();
        this.width[bufferWriteIndex] = texture.getWidth() * (source != null ? source.getWidth() / texture.getWidth() * scaleX : scaleX);
        this.height[bufferWriteIndex] = texture.getHeight() * (source != null ? source.getHeight() / texture.getHeight() * scaleY : scaleY);
        this.anchorX[bufferWriteIndex] = anchor.getX();
        this.anchorY[bufferWriteIndex] = anchor.getY();
        this.colorR[bufferWriteIndex] = color.getRed();
        this.colorG[bufferWriteIndex] = color.getGreen();
        this.colorB[bufferWriteIndex] = color.getBlue();
        this.colorA[bufferWriteIndex] = color.getAlpha();
        if (source != null) {
            this.hasSource[bufferWriteIndex] = true;
            this.sourceX[bufferWriteIndex] = source.getX();
            this.sourceY[bufferWriteIndex] = source.getY();
            this.sourceWidth[bufferWriteIndex] = source.getWidth();
            this.sourceHeight[bufferWriteIndex] = source.getHeight();
        } else {
            this.hasSource[bufferWriteIndex] = false;
        }
        this.rotation[bufferWriteIndex] = rotation;
        this.depth[bufferWriteIndex] = depth;

        bufferWriteIndex++;
    }

    @Override
    public void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color, Vector2 anchor,
                     float rotation, int depth) {
        if (bufferWriteIndex >= bufferMaxSize) {
            flush();
        }

        if (depth != 0) {
            hasDifferentDepthLevels = true;
        }

        this.textureId[bufferWriteIndex] = ((GLTexture) texture).getId();
        this.textureWidth[bufferWriteIndex] = texture.getWidth();
        this.textureHeight[bufferWriteIndex] = texture.getHeight();
        this.x[bufferWriteIndex] = displayArea.getX();
        this.y[bufferWriteIndex] = displayArea.getY();
        this.width[bufferWriteIndex] = displayArea.getWidth();
        this.height[bufferWriteIndex] = displayArea.getHeight();
        this.anchorX[bufferWriteIndex] = anchor.getX();
        this.anchorY[bufferWriteIndex] = anchor.getY();
        if (source != null) {
            this.hasSource[bufferWriteIndex] = true;
            this.sourceX[bufferWriteIndex] = source.getX();
            this.sourceY[bufferWriteIndex] = source.getY();
            this.sourceWidth[bufferWriteIndex] = source.getWidth();
            this.sourceHeight[bufferWriteIndex] = source.getHeight();
        } else {
            this.hasSource[bufferWriteIndex] = false;
        }
        this.colorR[bufferWriteIndex] = color.getRed();
        this.colorG[bufferWriteIndex] = color.getGreen();
        this.colorB[bufferWriteIndex] = color.getBlue();
        this.colorA[bufferWriteIndex] = color.getAlpha();
        this.rotation[bufferWriteIndex] = rotation;
        this.depth[bufferWriteIndex] = depth;

        bufferWriteIndex++;
    }

    @Override
    public void drawText(Font font, String text, Vector2 position, Color color, int fontSize) {
        float computedScale = fontSize / (float) font.getFontSize();
        float currentX = position.getX();
        float currentY = position.getY() + font.getFontSize() * computedScale + font.getVerticalSpacing();

        for (char ch : text.toCharArray()) {
            if (bufferWriteIndex >= bufferMaxSize) {
                flush();
            }

            FontGlyph glyph = font.getGlyph(ch);
            if (glyph == null) continue;

            if (ch == '\n') {
                currentY += font.getFontSize() * computedScale + font.getVerticalSpacing();
                currentX = position.getX();
                continue;
            }

            // Reusable glyph source is not needed anymore as source is broken down into primitives
            // reusableGlyphSource.set(glyph.getX(), glyph.getY(), glyph.getWidth(), glyph.getHeight());

            this.textureId[bufferWriteIndex] = ((GLFont) font).getTextureId();
            this.textureWidth[bufferWriteIndex] = ((GLFont) font).getTextureSize();
            this.textureHeight[bufferWriteIndex] = ((GLFont) font).getTextureSize();
            this.x[bufferWriteIndex] = currentX + glyph.getXOffset() * computedScale;
            this.y[bufferWriteIndex] = currentY + glyph.getYOffset() * computedScale;
            this.width[bufferWriteIndex] = glyph.getWidth() * computedScale;
            this.height[bufferWriteIndex] = glyph.getHeight() * computedScale;
            this.hasSource[bufferWriteIndex] = true;
            this.sourceX[bufferWriteIndex] = glyph.getX();
            this.sourceY[bufferWriteIndex] = glyph.getY();
            this.sourceWidth[bufferWriteIndex] = glyph.getWidth();
            this.sourceHeight[bufferWriteIndex] = glyph.getHeight();
            this.anchorX[bufferWriteIndex] = 0;
            this.anchorY[bufferWriteIndex] = 0;
            this.colorR[bufferWriteIndex] = color.getRed();
            this.colorG[bufferWriteIndex] = color.getGreen();
            this.colorB[bufferWriteIndex] = color.getBlue();
            this.colorA[bufferWriteIndex] = color.getAlpha();
            this.rotation[bufferWriteIndex] = 0f;
            this.depth[bufferWriteIndex] = 0; // Text is not depth-sorted for now

            currentX += glyph.getXAdvance() * computedScale + font.getHorizontalSpacing();
            bufferWriteIndex++;
        }
    }

    @Override
    public Shader getShader() {
        return shader;
    }

    @Override
    public void begin(Matrix4 viewMatrix) {
        this.begin(viewMatrix, BlendMode.NORMAL_BLEND);
    }

    @Override
    public void begin(Matrix4 viewMatrix, BlendMode blendMode) {
        bufferWriteIndex = 0;
        hasDifferentDepthLevels = false;

        if (blendMode == BlendMode.ADDITIVE) {
            glBlendFunc(GL_ONE, GL_ONE);
        } else if (blendMode == BlendMode.MULTIPLY) {
            glBlendFunc(GL_DST_COLOR, GL_ZERO);
        } else {
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        shader.bind();
        vao.bind();
        vbo.bind(GL_ARRAY_BUFFER);

        matrixBuffer.clear();
        viewMatrix.writeBuffer(matrixBuffer);
        glUniformMatrix4fv(shader.getUniformLocation("uMatrix"), false, matrixBuffer);
    }

    @Override
    public void end() {
        flush();
        vao.unbind();
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void resizeBuffer(int newSize) {
        if (newSize <= 0) {
            throw new RuntimeException("Invalid buffer size, must be greater than zero");
        }
        if (newSize == bufferMaxSize) return;

        this.bufferMaxSize = newSize;
        this.initBuffer();
    }

    private void initBuffer() {
        if (this.dataBuffer != null) {
            MemoryUtil.memFree(dataBuffer);
        }

        this.dataBuffer = MemoryUtil.memAllocFloat(SPRITE_UNIT_LENGTH * bufferMaxSize);

        textureId = new int[bufferMaxSize];
        depth = new int[bufferMaxSize];
        textureWidth = new int[bufferMaxSize];
        textureHeight = new int[bufferMaxSize];
        rotation = new float[bufferMaxSize];
        x = new float[bufferMaxSize];
        y = new float[bufferMaxSize];
        width = new float[bufferMaxSize];
        height = new float[bufferMaxSize];
        colorR = new float[bufferMaxSize];
        colorG = new float[bufferMaxSize];
        colorB = new float[bufferMaxSize];
        colorA = new float[bufferMaxSize];
        anchorX = new float[bufferMaxSize];
        anchorY = new float[bufferMaxSize];
        hasSource = new boolean[bufferMaxSize];
        sourceX = new float[bufferMaxSize];
        sourceY = new float[bufferMaxSize];
        sourceWidth = new float[bufferMaxSize];
        sourceHeight = new float[bufferMaxSize];
        depthSortIndices = new Integer[bufferMaxSize];

        for (int i = 0; i < bufferMaxSize; i++) {
            depthSortIndices[i] = i;
        }

        this.bufferWriteIndex = 0;
    }

    private void flush() {
        if (bufferWriteIndex == 0) return;

        if (hasDifferentDepthLevels) {
            Arrays.sort(this.depthSortIndices, 0, bufferWriteIndex, Comparator.comparingInt(i -> this.depth[i]));
        }

        int count = 0;
        int lastTexId = -1;
        shaderTextureMap.clear();
        dataBuffer.clear();

        for (int i = 0; i < bufferWriteIndex; ++i) {
            int index = hasDifferentDepthLevels ? depthSortIndices[i] : i;

            int currentTexId = this.textureId[index];
            if (currentTexId != lastTexId) {
                if (!shaderTextureMap.containsKey(currentTexId)) {
                    if (shaderTextureMap.size() >= shaderTextureCount) {
                        flushBatch(count);
                        count = 0;
                        dataBuffer.clear();
                        shaderTextureMap.clear();
                    }
                    int offset = shaderTextureMap.size();
                    glActiveTexture(GL_TEXTURE0 + offset);
                    glBindTexture(GL_TEXTURE_2D, currentTexId);
                    shaderTextureMap.put(currentTexId, offset);
                }
                lastTexId = currentTexId;
            }

            processSprite(index);
            count++;
        }

        flushBatch(count);
        bufferWriteIndex = 0;
    }

    private void flushBatch(int count) {
        if (count == 0) return;
        dataBuffer.flip();
        vbo.uploadData(GL_ARRAY_BUFFER, dataBuffer, GL_STREAM_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, 6 * count);
    }

    private void processSprite(int index) {
        spriteViewMatrix.setIdentity();
        spriteViewMatrix.translate(x[index] - width[index] * anchorX[index],
                y[index] - height[index] * anchorY[index], 0);
        if (rotation[index] != 0) {
            spriteViewMatrix.translate(width[index] * anchorX[index],
                    height[index] * anchorY[index], 0);
            spriteViewMatrix.rotate(rotation[index], 0f, 0f, 1.0f);
            spriteViewMatrix.translate(-width[index] * anchorX[index],
                    -height[index] * anchorY[index], 0);
        }
        spriteViewMatrix.scale(width[index], height[index], 0.0f);

        bottomLeft.set(0, 1);
        bottomLeft.transformMatrix4(spriteViewMatrix);
        bottomRight.set(1, 1);
        bottomRight.transformMatrix4(spriteViewMatrix);
        topLeft.set(0, 0);
        topLeft.transformMatrix4(spriteViewMatrix);
        topRight.set(1, 0);
        topRight.transformMatrix4(spriteViewMatrix);

        if (this.hasSource[index]) {
            float texW = this.textureWidth[index];
            float texH = this.textureHeight[index];
            tTopLeft.set(this.sourceX[index] / texW, this.sourceY[index] / texH);
            tTopRight.set((this.sourceX[index] + this.sourceWidth[index]) / texW, this.sourceY[index] / texH);
            tBottomRight.set((this.sourceX[index] + this.sourceWidth[index]) / texW, (this.sourceY[index] + this.sourceHeight[index]) / texH);
            tBottomLeft.set(this.sourceX[index] / texW, (this.sourceY[index] + this.sourceHeight[index]) / texH);
        } else {
            tTopLeft.set(0, 0);
            tTopRight.set(1, 0);
            tBottomRight.set(1, 1);
            tBottomLeft.set(0, 1);
        }

        int mappedTexId = shaderTextureCount == 1 ? 0 : shaderTextureMap.get(this.textureId[index]);

        uploadBufferData(bottomLeft.getX(), bottomLeft.getY(), tBottomLeft.getX(), tBottomLeft.getY(), index, mappedTexId);
        uploadBufferData(bottomRight.getX(), bottomRight.getY(), tBottomRight.getX(), tBottomRight.getY(), index, mappedTexId);
        uploadBufferData(topLeft.getX(), topLeft.getY(), tTopLeft.getX(), tTopLeft.getY(), index, mappedTexId);

        uploadBufferData(topLeft.getX(), topLeft.getY(), tTopLeft.getX(), tTopLeft.getY(), index, mappedTexId);
        uploadBufferData(bottomRight.getX(), bottomRight.getY(), tBottomRight.getX(), tBottomRight.getY(), index, mappedTexId);
        uploadBufferData(topRight.getX(), topRight.getY(), tTopRight.getX(), tTopRight.getY(), index, mappedTexId);
    }

    private void uploadBufferData(float x, float y, float tx, float ty, int index, int textureId) {
        this.dataBuffer.put(x);
        this.dataBuffer.put(y);
        this.dataBuffer.put(tx);
        this.dataBuffer.put(ty);
        this.dataBuffer.put(colorR[index]);
        this.dataBuffer.put(colorG[index]);
        this.dataBuffer.put(colorB[index]);
        this.dataBuffer.put(colorA[index]);
        this.dataBuffer.put(textureId);
    }
}
