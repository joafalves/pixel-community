package org.pixel.graphics.render;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL20C.glUniformMatrix4fv;

import java.nio.FloatBuffer;
import org.lwjgl.system.MemoryUtil;
import org.pixel.commons.logger.Logger;
import org.pixel.commons.logger.LoggerFactory;
import org.pixel.graphics.Color;
import org.pixel.graphics.PrimitiveType;
import org.pixel.graphics.shader.Shader;
import org.pixel.graphics.shader.VertexArrayObject;
import org.pixel.graphics.shader.VertexBufferObject;
import org.pixel.graphics.shader.standard.PrimitiveShader;
import org.pixel.math.Matrix4;
import org.pixel.math.Vector2;

public class PrimitiveBatch extends DrawBatch {

    private static final Logger log = LoggerFactory.getLogger(PrimitiveBatch.class);

    private static final int BUFFER_UNIT_LENGTH = 1024; // maximum vertices per batch
    private static final int VERT_UNIT_LENGTH = 16; // number of attribute information units per vertex
    private static final int ATTRIBUTE_STRIDE = 24; // attribute stride (bytes) between each vertex info

    private final VertexBufferObject vbo;
    private final VertexArrayObject vao;
    private final FloatBuffer dataBuffer;
    private final FloatBuffer matrixBuffer;

    private Shader shader;
    private int bufferMaxSize;
    private int bufferCount;
    private PrimitiveType primitiveType;
    private boolean running;

    /**
     * Constructor.
     */
    public PrimitiveBatch() {
        this(BUFFER_UNIT_LENGTH);
    }

    /**
     * Constructor.
     *
     * @param bufferMaxSize The maximum number of vertices that can be drawn in a single batch.
     */
    public PrimitiveBatch(int bufferMaxSize) {
        if (bufferMaxSize <= 0) {
            throw new RuntimeException("Invalid buffer size, must be greater than zero");
        }

        this.vbo = new VertexBufferObject();
        this.vao = new VertexArrayObject();
        this.matrixBuffer = MemoryUtil.memAllocFloat(4 * 4);
        this.dataBuffer = MemoryUtil.memAllocFloat(VERT_UNIT_LENGTH * bufferMaxSize);
        this.bufferMaxSize = bufferMaxSize;
        this.bufferCount = 0;

        log.trace("Buffer max size (units): '{}'.", this.bufferMaxSize);
        log.trace("Data buffer capacity: '{}'.", dataBuffer.capacity());

        this.init();
    }

    @Override
    public void begin(Matrix4 viewMatrix) {
        this.begin(viewMatrix, PrimitiveType.LINES);
    }

    public void begin(Matrix4 viewMatrix, PrimitiveType type) {
        primitiveType = type;
        dataBuffer.clear();
        bufferCount = 0;

        shader.use();
        vao.bind();
        vbo.bind(GL_ARRAY_BUFFER);

        matrixBuffer.clear();
        viewMatrix.writeBuffer(matrixBuffer);
        glUniformMatrix4fv(shader.getUniformLocation("uMatrix"), false, matrixBuffer);

        running = true;
    }

    @Override
    public void end() {
        flush();
        running = false;
    }

    @Override
    public void dispose() {
        shader.dispose();
        vbo.dispose();
        vao.dispose();
    }

    /**
     * Draw vertex at the specified position and color.
     *
     * @param position The position of the vertex.
     * @param color    The color of the vertex.
     */
    public void draw(Vector2 position, Color color) {
        this.draw(position.getX(), position.getY(), color);
    }

    /**
     * Draw vertex at the specified position and color.
     *
     * @param x     The x position of the vertex.
     * @param y     The y position of the vertex.
     * @param color The color of the vertex.
     */
    public void draw(float x, float y, Color color) {
        dataBuffer.put(x);
        dataBuffer.put(y);
        dataBuffer.put(color.getRed());
        dataBuffer.put(color.getGreen());
        dataBuffer.put(color.getBlue());
        dataBuffer.put(color.getAlpha());

        vertexAdded();
    }

    private void vertexAdded() {
        if (++bufferCount >= bufferMaxSize) {
            flush();
        }
    }

    private void init() {
        shader = new PrimitiveShader();
        shader.use();

        // setup attributes:
        int aVertexPosition = this.shader.getAttributeLocation("aVertexPosition");
        int aVertexColor = this.shader.getAttributeLocation("aVertexColor");

        vao.bind();
        vbo.bind(GL_ARRAY_BUFFER);

        glEnableVertexAttribArray(aVertexPosition);
        glVertexAttribPointer(aVertexPosition, 2, GL_FLOAT, false, ATTRIBUTE_STRIDE, 0);

        glEnableVertexAttribArray(aVertexColor);
        glVertexAttribPointer(aVertexColor, 4, GL_FLOAT, false, ATTRIBUTE_STRIDE, 2 * Float.BYTES);
    }

    private void flush() {
        dataBuffer.flip();
        vbo.uploadData(GL_ARRAY_BUFFER, dataBuffer, GL_STATIC_DRAW);
        glDrawArrays(primitiveType.getNativeMode(), 0, bufferCount);

        bufferCount = 0;
        dataBuffer.clear();
    }

    /**
     * Get the maximum buffer size.
     *
     * @return The maximum buffer size.
     */
    public int getBufferSize() {
        return bufferMaxSize;
    }

    /**
     * Set the maximum buffer size.
     *
     * @param bufferMaxSize The maximum number of vertices that can be drawn in a single batch.
     */
    public void setBufferSize(int bufferMaxSize) {
        if (running) {
            log.warn("Cannot change buffer size while drawing, discarding...");
            return;
        }

        this.bufferMaxSize = bufferMaxSize;
    }
}
