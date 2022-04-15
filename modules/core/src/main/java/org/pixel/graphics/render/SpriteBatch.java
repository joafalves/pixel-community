/*
 * This software is available under Apache License
 * Copyright (c) 2020
 */

package org.pixel.graphics.render;

import org.lwjgl.system.MemoryUtil;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.Font;
import org.pixel.content.FontGlyph;
import org.pixel.content.Texture;
import org.pixel.graphics.Color;
import org.pixel.graphics.shader.Shader;
import org.pixel.graphics.shader.VertexArrayObject;
import org.pixel.graphics.shader.VertexBufferObject;
import org.pixel.graphics.shader.standard.MultiTextureShader;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11C.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13C.GL_DST_COLOR;
import static org.lwjgl.opengl.GL13C.GL_FLOAT;
import static org.lwjgl.opengl.GL13C.GL_ONE;
import static org.lwjgl.opengl.GL13C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL13C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL13C.GL_ZERO;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL13C.glBindTexture;
import static org.lwjgl.opengl.GL13C.glBlendFunc;
import static org.lwjgl.opengl.GL13C.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glGetIntegerv;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20C.glUniformMatrix4fv;

public class SpriteBatch extends DrawBatch {

    //region private properties

    private static final Logger log = LoggerFactory.getLogger(SpriteBatch.class);

    private static final int BUFFER_UNIT_LENGTH = 256; // maximum sprites per batch
    private static final int SPRITE_UNIT_LENGTH = 54; // number of attributes information units per sprite (uploadBufferData * each inner put)
    private static final int ATTRIBUTE_STRIDE = 36; // attribute stride (bytes) between each vertex info

    private static final Matrix4 spriteViewMatrix = new Matrix4();
    private static final Vector2 tTopLeft = new Vector2();
    private static final Vector2 tTopRight = new Vector2();
    private static final Vector2 tBottomRight = new Vector2();
    private static final Vector2 tBottomLeft = new Vector2();
    private static final Vector2 bottomLeft = new Vector2();
    private static final Vector2 bottomRight = new Vector2();
    private static final Vector2 topLeft = new Vector2();
    private static final Vector2 topRight = new Vector2();

    private final HashMap<Integer, Integer> shaderTextureMap = new HashMap<>();
    private final VertexBufferObject vbo;
    private final VertexArrayObject vao;
    private final FloatBuffer dataBuffer;
    private final FloatBuffer matrixBuffer;
    private final SpriteData[] spriteData;
    private final Vector2 anchorZero = Vector2.zero();
    private final int bufferMaxSize;
    private final int shaderTextureCount;

    private Shader shader;
    private int bufferCount;
    private int lastTextureId;
    private int lastDepthLevel;
    private boolean hasDifferentDepthLevels;

    //endregion

    //region constructors

    /**
     * Constructor.
     */
    public SpriteBatch() {
        this(BUFFER_UNIT_LENGTH);
    }

    /**
     * Constructor.
     *
     * @param bufferMaxSize The maximum number of sprites that can be drawn in one batch.
     */
    public SpriteBatch(int bufferMaxSize) {
        this(bufferMaxSize, 0);
    }

    /**
     * Constructor.
     *
     * @param bufferMaxSize      The maximum number of sprites that can be drawn in one batch.
     * @param shaderTextureCount The number of textures to be used by the shader (if the parameter is set to '0', the
     *                           value will be set based on the device maximum capacity).
     */
    public SpriteBatch(int bufferMaxSize, int shaderTextureCount) {
        if (bufferMaxSize <= 0) {
            throw new RuntimeException("Invalid buffer size, must be greater than zero");
        }

        this.vbo = new VertexBufferObject();
        this.matrixBuffer = MemoryUtil.memAllocFloat(4 * 4);
        this.spriteData = new SpriteData[bufferMaxSize];
        this.bufferMaxSize = bufferMaxSize;
        this.dataBuffer = MemoryUtil.memAllocFloat(SPRITE_UNIT_LENGTH * bufferMaxSize);
        this.vao = new VertexArrayObject();
        this.bufferCount = 0;

        if (shaderTextureCount <= 0) {
            log.trace("Setting max number of textures based on 'GL_MAX_TEXTURE_IMAGE_UNITS'.");

            int[] textureUnits = new int[1];
            glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, textureUnits);
            this.shaderTextureCount = textureUnits[0] > 0 ? textureUnits[0] : 1;

        } else {
            this.shaderTextureCount = shaderTextureCount;
        }

        log.trace("Buffer max size (units): '{}'.", this.bufferMaxSize);
        log.trace("Shader texture count: '{}'.", this.shaderTextureCount);
        log.trace("Data buffer capacity: '{}'.", dataBuffer.capacity());

        this.init();
    }

    //endregion

    //region private methods

    private void init() {
        shader = new MultiTextureShader(shaderTextureCount);
        shader.use();

        // since the base texture is always TEXTURE0, this can be done only once (for the 'shaderTextureCount' amount)
        int[] textureRefArray = new int[shaderTextureCount];
        for (int i = 0; i < shaderTextureCount; i++) {
            textureRefArray[i] = i;
        }
        glUniform1iv(shader.getUniformLocation("uTextureImage"), textureRefArray);

        // setup attributes:
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

        // initialize sprite data objects
        for (int i = 0; i < bufferMaxSize; i++) {
            this.spriteData[i] = new SpriteData();
        }
    }

    private SpriteData getNextSpriteDataObject() {
        return this.spriteData[this.bufferCount++];
    }

    private void putTexture(SpriteData spriteData) {
        int offset = shaderTextureMap.size();
        glActiveTexture(GL_TEXTURE0 + offset);
        glBindTexture(GL_TEXTURE_2D, spriteData.textureId);
        shaderTextureMap.put(spriteData.textureId, offset);
    }

    private void flushBatch(int count) {
        dataBuffer.flip();
        vbo.uploadData(GL_ARRAY_BUFFER, dataBuffer, GL_STATIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, 6 * count);
    }

    private void flush() {
        if (hasDifferentDepthLevels) {
            Arrays.sort(this.spriteData, (o1, o2) -> {
                if (!o1.active || !o2.active) {
                    return Boolean.compare(o2.active, o1.active);
                }

                return o1.depth - o2.depth;
            });
        }

        // draw the sprite data..
        int count = 0;
        for (int i = 0; i < bufferCount; ++i) {
            SpriteData spriteData = this.spriteData[i];
            if (!spriteData.active) {
                continue; // skip inactive sprites
            }

            if (lastTextureId != spriteData.textureId) {
                if (!shaderTextureMap.containsKey(spriteData.textureId)) {
                    if (shaderTextureMap.size() >= shaderTextureCount) {
                        flushBatch(count);
                        count = 0;
                        dataBuffer.clear();
                        shaderTextureMap.clear();
                    }
                    putTexture(spriteData);
                }

                lastTextureId = spriteData.textureId;
            }

            processSpriteData(spriteData);
            count++;
            spriteData.active = false;
        }

        flushBatch(count);
        dataBuffer.clear();

        hasDifferentDepthLevels = false;
        lastDepthLevel = -1;
        bufferCount = 0;
    }

    private void processSpriteData(SpriteData sprite) {
        // compute the sprite visualization matrix (transform the vertices according to the sprite characteristics)
        sprite.computeViewMatrix();

        // note that both position and source data have the following coordinate orientation:
        // (this depends on how the texture is loaded into memory, TextureImporter will read topLeft to bottomRight)
        // ##############
        // #(0,0)..(1,0)#
        // #....\.......#
        // #..A..\...B..#
        // #......\.....#
        // #(0,1)..(1,1)#
        // ##############

        // vertex data:
        bottomLeft.set(0, 1);
        bottomLeft.transformMatrix4(spriteViewMatrix);
        bottomRight.set(1);
        bottomRight.transformMatrix4(spriteViewMatrix);
        topLeft.set(0);
        topLeft.transformMatrix4(spriteViewMatrix);
        topRight.set(1, 0);
        topRight.transformMatrix4(spriteViewMatrix);

        // texture source:
        tTopLeft.set(0);
        tTopRight.set(1, 0);
        tBottomRight.set(1);
        tBottomLeft.set(0, 1);
        if (sprite.source != null) {
            // use has defined custom source area; the org.pixel.input is relative to the real width and height of
            // the source texture, therefore we need to convert into space area (x=[0-1];y=[0-1])
            tTopLeft.set((sprite.source.getX() / sprite.textureWidth),
                    (sprite.source.getY() / sprite.textureHeight));
            tTopRight.set(((sprite.source.getX() + sprite.source.getWidth()) / sprite.textureWidth),
                    (sprite.source.getY() / sprite.textureHeight));
            tBottomRight.set(((sprite.source.getX() + sprite.source.getWidth()) / sprite.textureWidth),
                    ((sprite.source.getY() + sprite.source.getHeight()) / sprite.textureHeight));
            tBottomLeft.set((sprite.source.getX() / sprite.textureWidth),
                    ((sprite.source.getY() + sprite.source.getHeight()) / sprite.textureHeight));
        }

        int textureId = shaderTextureCount == 1 ? 0 : shaderTextureMap.get(sprite.textureId);

        // put the drawing data on the buffer:
        this.uploadTriangleData(bottomLeft, bottomRight, topLeft, tBottomLeft, tBottomRight, tTopLeft, sprite.color,
                textureId);
        this.uploadTriangleData(topLeft, bottomRight, topRight, tTopLeft, tBottomRight, tTopRight, sprite.color,
                textureId);
    }

    private void uploadTriangleData(Vector2 v1, Vector2 v2, Vector2 v3, Vector2 t1, Vector2 t2, Vector2 t3, Color color,
            int textureId) {
        this.uploadBufferData(v1.getX(), v1.getY(), t1.getX(), t1.getY(), color, textureId);
        this.uploadBufferData(v2.getX(), v2.getY(), t2.getX(), t2.getY(), color, textureId);
        this.uploadBufferData(v3.getX(), v3.getY(), t3.getX(), t3.getY(), color, textureId);
    }

    private void uploadBufferData(float x, float y, float px, float py, Color color, int textureId) {
        this.dataBuffer.put(x);
        this.dataBuffer.put(y);
        this.dataBuffer.put(px);
        this.dataBuffer.put(py);
        this.dataBuffer.put(color.getRed());
        this.dataBuffer.put(color.getGreen());
        this.dataBuffer.put(color.getBlue());
        this.dataBuffer.put(color.getAlpha());
        this.dataBuffer.put(textureId);
    }

    private void spriteDataAdded() {
        if (bufferCount >= bufferMaxSize) {
            flush();
        }
    }

    //endregion

    //region public methods

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     */
    public void draw(Texture texture, Vector2 position) {
        draw(texture, position, Color.WHITE);
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param color    The color overlay of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Color color) {
        draw(texture, position, color, anchorZero, 1.0f);
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param color    The color overlay of the sprite.
     * @param anchor   The anchor point of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Color color, Vector2 anchor) {
        draw(texture, position, color, anchor, 1.0f);
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param color    The color overlay of the sprite.
     * @param anchor   The anchor point of the sprite.
     * @param scale    The scale of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Color color, Vector2 anchor, float scale) {
        draw(texture, position, color, anchor, scale, scale, 0.0f);
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param color    The color overlay of the sprite.
     * @param anchor   The anchor point of the sprite.
     * @param scaleX   The scale of the sprite on the x-axis.
     * @param scaleY   The scale of the sprite on the y-axis.
     * @param rotation The rotation of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Color color, Vector2 anchor, float scaleX, float scaleY,
            float rotation) {
        this.draw(texture, position, null, color, anchor, scaleX, scaleY, rotation, 0);
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param source   The source rectangle of the sprite.
     * @param color    The color overlay of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color) {
        this.draw(texture, position, source, color, anchorZero, 1.0f, 1.0f, 0.f, 0);
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param source   The source rectangle of the sprite.
     * @param color    The color overlay of the sprite.
     * @param anchor   The anchor point of the sprite.
     * @param scale    The scale of the sprite.
     * @param rotation The rotation of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scale,
            float rotation) {
        this.draw(texture, position, source, color, anchor, scale, scale, rotation, 0);
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param source   The source rectangle of the sprite.
     * @param color    The color overlay of the sprite.
     * @param anchor   The anchor point of the sprite.
     * @param scaleX   The scale of the sprite on the x-axis.
     * @param scaleY   The scale of the sprite on the y-axis.
     * @param rotation The rotation of the sprite.
     */
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scaleX,
            float scaleY, float rotation) {
        this.draw(texture, position, source, color, anchor, scaleX, scaleY, rotation, 0);
    }

    /**
     * Draws a sprite.
     *
     * @param texture  The texture to use.
     * @param position The position of the sprite.
     * @param source   The source rectangle of the sprite.
     * @param color    The color overlay of the sprite.
     * @param anchor   The anchor point of the sprite.
     * @param scaleX   The scale of the sprite on the x-axis.
     * @param scaleY   The scale of the sprite on the y-axis.
     * @param rotation The rotation of the sprite.
     * @param depth    The drawing depth of the sprite (lower numbers are drawn first).
     */
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scaleX,
            float scaleY, float rotation, int depth) {
        if (lastDepthLevel >= 0 && depth != lastDepthLevel) {
            hasDifferentDepthLevels = true;
        }

        SpriteData spriteData = getNextSpriteDataObject();
        spriteData.active = true;
        spriteData.textureId = texture.getId();
        spriteData.textureWidth = texture.getWidth();
        spriteData.textureHeight = texture.getHeight();
        spriteData.x = position.getX();
        spriteData.y = position.getY();
        spriteData.width =
                texture.getWidth() * (source != null ? source.getWidth() / texture.getWidth() * scaleX : scaleX);
        spriteData.height =
                texture.getHeight() * (source != null ? source.getHeight() / texture.getHeight() * scaleY : scaleY);
        spriteData.anchor = anchor;
        spriteData.color = color;
        spriteData.source = source;
        spriteData.rotation = rotation;
        spriteData.depth = depth;

        lastDepthLevel = depth;

        spriteDataAdded();
    }

    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     */
    public void draw(Texture texture, Rectangle displayArea) {
        this.draw(texture, displayArea, Color.WHITE, Vector2.ZERO, 0.f);
    }

    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     * @param color       The color overlay of the sprite.
     */
    public void draw(Texture texture, Rectangle displayArea, Color color) {
        this.draw(texture, displayArea, color, Vector2.ZERO, 0.f);
    }

    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     * @param color       The color overlay of the sprite.
     * @param anchor      The anchor point of the sprite.
     * @param rotation    The rotation of the sprite.
     */
    public void draw(Texture texture, Rectangle displayArea, Color color, Vector2 anchor, float rotation) {
        this.draw(texture, displayArea, null, color, anchor, rotation);
    }

    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     * @param source      The source rectangle of the sprite.
     * @param color       The color overlay of the sprite.
     */
    public void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color) {
        this.draw(texture, displayArea, source, color, Vector2.ZERO, 0f);
    }

    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     * @param source      The source rectangle of the sprite.
     * @param color       The color overlay of the sprite.
     * @param anchor      The anchor point of the sprite.
     * @param rotation    The rotation of the sprite.
     */
    public void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color, Vector2 anchor,
            float rotation) {
        this.draw(texture, displayArea, source, color, anchor, rotation, 0);
    }

    /**
     * Draws a sprite.
     *
     * @param texture     The texture to use.
     * @param displayArea The display area of the sprite.
     * @param source      The source rectangle of the sprite.
     * @param color       The color overlay of the sprite.
     * @param anchor      The anchor point of the sprite.
     * @param rotation    The rotation of the sprite.
     * @param depth       The drawing depth of the sprite (lower numbers are drawn first).
     */
    public void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color, Vector2 anchor,
            float rotation, int depth) {
        if (lastDepthLevel >= 0 && depth != lastDepthLevel) {
            hasDifferentDepthLevels = true;
        }

        SpriteData spriteData = getNextSpriteDataObject();
        spriteData.active = true;
        spriteData.textureId = texture.getId();
        spriteData.textureWidth = texture.getWidth();
        spriteData.textureHeight = texture.getHeight();
        spriteData.x = displayArea.getX();
        spriteData.y = displayArea.getY();
        spriteData.width = displayArea.getWidth();
        spriteData.height = displayArea.getHeight();
        spriteData.anchor = anchor;
        spriteData.source = source;
        spriteData.color = color;
        spriteData.rotation = rotation;
        spriteData.depth = depth;

        lastDepthLevel = depth;

        spriteDataAdded();
    }

    /**
     * Draws text.
     *
     * @param font     The font to use.
     * @param text     The text to draw.
     * @param position The position of the text.
     * @param color    The color of the text.
     */
    public void drawText(Font font, String text, Vector2 position, Color color) {
        this.drawText(font, text, position, color, font.getFontSize());
    }

    /**
     * Draws text.
     *
     * @param font     The font to use.
     * @param text     The text to draw.
     * @param position The position of the text.
     * @param color    The color of the text.
     * @param fontSize The size of the font.
     */
    public void drawText(Font font, String text, Vector2 position, Color color, int fontSize) {
        // we are going to create a sprite data for each text character:
        float computedScale = fontSize / (float) font.getComputedFontSize();
        float scale = fontSize / (float) font.getFontSize();
        int x = (int) position.getX(); // initial x position
        int y = (int) (position.getY() + font.getFontSize() * scale + font.getVerticalSpacing());
        for (char ch : text.toCharArray()) {
            FontGlyph glyph = font.getGlyph(ch);
            if (glyph == null) {
                continue; // cannot process this char data...
            }

            if (ch == '\n') {
                y += font.getFontSize() * scale + font.getVerticalSpacing();
                x = (int) position.getX();
                continue;
            }

            SpriteData spriteData = getNextSpriteDataObject();
            spriteData.active = true;
            spriteData.textureId = font.getTextureId();
            spriteData.textureWidth = font.getTextureWidth();
            spriteData.textureHeight = font.getTextureHeight();
            spriteData.x = x + glyph.getXOffset() * scale;
            spriteData.y = y + glyph.getYOffset() * scale;
            spriteData.width = glyph.getWidth() * computedScale;
            spriteData.height = glyph.getHeight() * computedScale;
            spriteData.source = new Rectangle(glyph.getX(), glyph.getY(), glyph.getWidth(), glyph.getHeight());
            spriteData.anchor = Vector2.zero();
            spriteData.color = color;
            spriteData.rotation = 0f;

            x += glyph.getXAdvance() * scale + font.getHorizontalSpacing();

            spriteDataAdded();
        }
    }

    /**
     * Get the active shader.
     *
     * @return The active shader.
     */
    public Shader getShader() {
        return shader;
    }

    /**
     * Begin drawing phase.
     *
     * @param viewMatrix The view matrix.
     */
    @Override
    public void begin(Matrix4 viewMatrix) {
        this.begin(viewMatrix, BlendMode.NORMAL_BLEND);
    }

    /**
     * Begin drawing phase.
     *
     * @param viewMatrix The view matrix.
     * @param blendMode  The blend mode.
     */
    public void begin(Matrix4 viewMatrix, BlendMode blendMode) {
        dataBuffer.clear();
        shaderTextureMap.clear();
        bufferCount = 0;
        lastTextureId = -1;
        lastDepthLevel = -1;
        hasDifferentDepthLevels = false;

        if (blendMode == BlendMode.ADDITIVE) {
            glBlendFunc(GL_ONE, GL_ONE);
        } else if (blendMode == BlendMode.MULTIPLY) {
            glBlendFunc(GL_DST_COLOR, GL_ZERO);
        } else {
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        // use shader
        shader.use();

        // bind buffers
        vao.bind();
        vbo.bind(GL_ARRAY_BUFFER);

        // apply camera matrix
        matrixBuffer.clear();
        viewMatrix.writeBuffer(matrixBuffer);
        glUniformMatrix4fv(shader.getUniformLocation("uMatrix"), false, matrixBuffer);
    }

    /**
     * End drawing phase.
     */
    @Override
    public void end() {
        flush();

        vao.unbind();

        // restore global blend func
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void dispose() {
        shader.dispose();
        vbo.dispose();
        vao.dispose();
    }

    //endregion

    //region private classes

    /**
     * SpriteData class
     */
    private static class SpriteData {

        boolean active;
        int textureId;
        int depth;
        float textureWidth;
        float textureHeight;
        float rotation;
        float x;
        float y;
        float width;
        float height;
        Color color;
        Vector2 anchor;
        Rectangle source; // texture source area

        void computeViewMatrix() {
            // reset
            spriteViewMatrix.setIdentity();

            // position:
            spriteViewMatrix.translate(x - width * anchor.getX(), y - height * anchor.getY(), 0);

            // rotation:
            if (rotation != 0) {
                spriteViewMatrix.translate(width * anchor.getX(), height * anchor.getY(), 0);
                spriteViewMatrix.rotate(rotation, 0f, 0f, 1.0f);
                spriteViewMatrix.translate(-width * anchor.getX(), -height * anchor.getY(), 0);
            }

            // scale:
            spriteViewMatrix.scale(width, height, 0.0f);
        }
    }

    //endregion
}
