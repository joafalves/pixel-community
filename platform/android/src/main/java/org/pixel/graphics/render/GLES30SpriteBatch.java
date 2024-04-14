package org.pixel.graphics.render;

import android.opengl.GLES30;
import org.pixel.commons.Color;
import org.pixel.commons.lifecycle.State;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.content.Font;
import org.pixel.content.Texture;
import org.pixel.graphics.shader.Shader;
import org.pixel.graphics.shader.opengl.GLES30Shader;
import org.pixel.graphics.shader.opengl.GLES30TextureShader;
import org.pixel.graphics.shader.opengl.GLES30VertexArrayObject;
import org.pixel.graphics.shader.opengl.GLES30VertexBufferObject;
import org.pixel.math.Matrix4;
import org.pixel.math.Rectangle;
import org.pixel.math.Vector2;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class GLES30SpriteBatch extends SpriteBatch {

    private static final Logger log = LoggerFactory.getLogger(GLES30SpriteBatch.class);

    private static final int BUFFER_UNIT_LENGTH = 256;
    private static final int SPRITE_UNIT_LENGTH = 48; // This might be adjusted based on actual attributes count and size
    private static final int ATTRIBUTE_STRIDE = 32; // This should match the number of bytes per vertex attributes set

    private static final Matrix4 spriteViewMatrix = new Matrix4();
    private final Vector2 tTopLeft = new Vector2();
    private final Vector2 tTopRight = new Vector2();
    private final Vector2 tBottomRight = new Vector2();
    private final Vector2 tBottomLeft = new Vector2();
    private final Vector2 bottomLeft = new Vector2();
    private final Vector2 bottomRight = new Vector2();
    private final Vector2 topLeft = new Vector2();
    private final Vector2 topRight = new Vector2();

    private State state = State.CREATED;
    private GLES30VertexBufferObject vbo;
    private GLES30VertexArrayObject vao;
    private GLES30Shader shader;
    private FloatBuffer matrixBuffer;
    private FloatBuffer dataBuffer;
    private SpriteData[] spriteData;

    private int bufferMaxSize;
    private int bufferWriteIndex;
    private int lastTextureId;
    private int lastDepthLevel;
    private boolean hasDifferentDepthLevels;

    /**
     * Constructor.
     */
    public GLES30SpriteBatch() {
        this(BUFFER_UNIT_LENGTH);
    }

    /**
     * Constructor.
     *
     * @param bufferMaxSize The maximum buffer size.
     */
    public GLES30SpriteBatch(int bufferMaxSize) {
        this.bufferMaxSize = bufferMaxSize;
        this.bufferWriteIndex = 0;
    }

    @Override
    public boolean init() {
        if (state.hasInitialized()) {
            log.warn("SpriteBatch already initialized.");
            return false;
        }
        state = State.INITIALIZING;

        vbo = new GLES30VertexBufferObject();
        vao = new GLES30VertexArrayObject();
        shader = new GLES30TextureShader();

        initShader();
        initBuffer();

        state = State.INITIALIZED;
        return true;
    }

    @Override
    public void draw(Texture texture, Vector2 position, Rectangle source, Color color, Vector2 anchor, float scaleX, float scaleY, float rotation, int depth) {
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
        spriteData.width = texture.getWidth()
                * (source != null ? source.getWidth() / texture.getWidth() * scaleX : scaleX);
        spriteData.height = texture.getHeight()
                * (source != null ? source.getHeight() / texture.getHeight() * scaleY : scaleY);
        spriteData.anchor = anchor;
        spriteData.color = color;
        spriteData.source = source;
        spriteData.rotation = rotation;
        spriteData.depth = depth;

        lastDepthLevel = depth;

        spriteDataAdded();
    }

    @Override
    public void draw(Texture texture, Rectangle displayArea, Rectangle source, Color color, Vector2 anchor, float rotation, int depth) {
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

    @Override
    public void drawText(Font font, String text, Vector2 position, Color color, int fontSize) {
        throw new UnsupportedOperationException("Not implemented yet.");
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
        dataBuffer.clear();
        bufferWriteIndex = 0;
        lastTextureId = -1;
        lastDepthLevel = -1;
        hasDifferentDepthLevels = false;

        if (blendMode == BlendMode.ADDITIVE) {
            GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ONE);
        } else if (blendMode == BlendMode.MULTIPLY) {
            GLES30.glBlendFunc(GLES30.GL_DST_COLOR, GLES30.GL_ZERO);
        } else {
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
        }

        // use shader
        shader.use();

        // bind buffers
        vao.bind();
        vbo.bind(GLES30.GL_ARRAY_BUFFER);

        // apply camera matrix
        matrixBuffer.clear();
        viewMatrix.writeBuffer(matrixBuffer);
        GLES30.glUniformMatrix4fv(shader.getUniformLocation("uMatrix"), 1, false, matrixBuffer);
    }

    @Override
    public void end() {
        flush();

        vao.unbind();

        // restore global blend func
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void resizeBuffer(int newSize) {
        if (newSize <= 0) {
            throw new RuntimeException("Invalid buffer size, must be greater than zero");
        }

        if (newSize == bufferMaxSize) {
            return; // no need to resize
        }

        this.bufferMaxSize = newSize;
        this.initBuffer();
    }

    @Override
    public void dispose() {
        if (state == State.DISPOSED) {
            log.warn("SpriteBatch already disposed.");
            return;
        }
        state = State.DISPOSING;

        vbo.dispose();
        vao.dispose();
        shader.dispose();

        state = State.DISPOSED;
    }

    private void initShader() {
        shader.use();

        // setup attributes:
        int aVertexPosition = this.shader.getAttributeLocation("aVertexPosition");
        int aTextureCoordinates = this.shader.getAttributeLocation("aTextureCoordinates");
        int aVertexColor = this.shader.getAttributeLocation("aVertexColor");

        vao.bind();
        vbo.bind(GLES30.GL_ARRAY_BUFFER);

        GLES30.glEnableVertexAttribArray(aVertexPosition);
        GLES30.glVertexAttribPointer(aVertexPosition, 2, GLES30.GL_FLOAT, false, ATTRIBUTE_STRIDE, 0);

        GLES30.glEnableVertexAttribArray(aTextureCoordinates);
        GLES30.glVertexAttribPointer(aTextureCoordinates, 2, GLES30.GL_FLOAT, false, ATTRIBUTE_STRIDE, 2 * Float.BYTES);

        GLES30.glEnableVertexAttribArray(aVertexColor);
        GLES30.glVertexAttribPointer(aVertexColor, 4, GLES30.GL_FLOAT, false, ATTRIBUTE_STRIDE, 4 * Float.BYTES);
    }

    private void initBuffer() {
        this.matrixBuffer = FloatBuffer.allocate(16);
        this.dataBuffer = FloatBuffer.allocate(bufferMaxSize * SPRITE_UNIT_LENGTH);
        this.bufferWriteIndex = 0; // Ensure the buffer write index is reset because the buffer size has changed

        // initialize sprite data objects
        this.spriteData = new SpriteData[bufferMaxSize];
        for (int i = 0; i < bufferMaxSize; i++) {
            this.spriteData[i] = new SpriteData();
        }
    }

    private SpriteData getNextSpriteDataObject() {
        return this.spriteData[this.bufferWriteIndex++];
    }

    private void spriteDataAdded() {
        if (bufferWriteIndex >= bufferMaxSize) {
            flush();
        }
    }

    private void flushBatch(int count) {
        dataBuffer.flip();
        vbo.uploadData(GLES30.GL_ARRAY_BUFFER, dataBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6 * count);
        dataBuffer.clear();
        int error = GLES30.glGetError();
        if (error != GLES30.GL_NO_ERROR) {
           log.error("Error while drawing sprites: " + error);
        }
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
        for (int i = 0; i < bufferWriteIndex; ++i) {
            SpriteData spriteData = this.spriteData[i];
            if (!spriteData.active) {
                continue; // skip inactive sprites
            }

            // texture checkup:
            if (lastTextureId != spriteData.textureId) {
                // is there any previous texture id?
                if (lastTextureId >= 0) {
                    // yes, which means we have something to render...
                    flushBatch(count);
                    count = 0;
                }

                // bind the new texture:
                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, spriteData.textureId);
                lastTextureId = spriteData.textureId;
            }

            processSpriteData(spriteData);
            count++;
            spriteData.active = false;
        }

        flushBatch(count);

        hasDifferentDepthLevels = false;
        lastDepthLevel = -1;
        bufferWriteIndex = 0;
    }

    private void processSpriteData(SpriteData sprite) {
        // compute the sprite visualization matrix (transform the vertices according to
        // the sprite characteristics)
        computeSpriteDataViewMatrix(sprite);

        // note that both position and source data have the following coordinate
        // orientation:
        // (this depends on how the texture is loaded into memory, TextureImporter will
        // read topLeft to bottomRight)
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
            // use has defined custom source area; the org.pixel.input is relative to the
            // real width and height of
            // the source texture, therefore we need to convert into space area
            // (x=[0-1];y=[0-1])
            tTopLeft.set((sprite.source.getX() / sprite.textureWidth),
                    (sprite.source.getY() / sprite.textureHeight));
            tTopRight.set(((sprite.source.getX() + sprite.source.getWidth()) / sprite.textureWidth),
                    (sprite.source.getY() / sprite.textureHeight));
            tBottomRight.set(((sprite.source.getX() + sprite.source.getWidth()) / sprite.textureWidth),
                    ((sprite.source.getY() + sprite.source.getHeight()) / sprite.textureHeight));
            tBottomLeft.set((sprite.source.getX() / sprite.textureWidth),
                    ((sprite.source.getY() + sprite.source.getHeight()) / sprite.textureHeight));
        }

        // put the drawing data on the buffer:
        this.uploadTriangleData(bottomLeft, bottomRight, topLeft, tBottomLeft, tBottomRight, tTopLeft, sprite.color);
        this.uploadTriangleData(topLeft, bottomRight, topRight, tTopLeft, tBottomRight, tTopRight, sprite.color);
    }

    private void uploadTriangleData(Vector2 v1, Vector2 v2, Vector2 v3, Vector2 t1, Vector2 t2, Vector2 t3, Color color) {
        this.uploadBufferData(v1.getX(), v1.getY(), t1.getX(), t1.getY(), color);
        this.uploadBufferData(v2.getX(), v2.getY(), t2.getX(), t2.getY(), color);
        this.uploadBufferData(v3.getX(), v3.getY(), t3.getX(), t3.getY(), color);
    }

    private void computeSpriteDataViewMatrix(SpriteData spriteData) {
        // reset
        spriteViewMatrix.setIdentity();

        // position:
        spriteViewMatrix.translate(spriteData.x - spriteData.width * spriteData.anchor.getX(),
                spriteData.y - spriteData.height * spriteData.anchor.getY(), 0);

        // rotation:
        if (spriteData.rotation != 0) {
            spriteViewMatrix.translate(spriteData.width * spriteData.anchor.getX(),
                    spriteData.height * spriteData.anchor.getY(), 0);
            spriteViewMatrix.rotate(spriteData.rotation, 0f, 0f, 1.0f);
            spriteViewMatrix.translate(-spriteData.width * spriteData.anchor.getX(),
                    -spriteData.height * spriteData.anchor.getY(), 0);
        }

        // scale:
        spriteViewMatrix.scale(spriteData.width, spriteData.height, 0.0f);
    }

    private void uploadBufferData(float x, float y, float px, float py, Color color) {
        this.dataBuffer.put(x);
        this.dataBuffer.put(y);
        this.dataBuffer.put(px);
        this.dataBuffer.put(py);
        this.dataBuffer.put(color.getRed());
        this.dataBuffer.put(color.getGreen());
        this.dataBuffer.put(color.getBlue());
        this.dataBuffer.put(color.getAlpha());
    }

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
    }
}
