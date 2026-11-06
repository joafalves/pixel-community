package org.pixel.graphics.backend;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * CPU-side vertex data of one pass, ready to be uploaded by the backend.
 * <p>
 * Backed by a direct, native-order buffer that grows (doubling) but never shrinks, so steady-state frames don't
 * allocate. Quads are written as 4 vertices (top-left, top-right, bottom-right, bottom-left); the backend draws them
 * with a shared index pattern.
 */
public final class VertexStream {

    private static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

    private final VertexLayout layout;
    private ByteBuffer buffer;
    private int vertexCount;

    /**
     * Constructor.
     *
     * @param layout          The vertex layout.
     * @param initialVertices Initial capacity in vertices.
     */
    public VertexStream(VertexLayout layout, int initialVertices) {
        this.layout = layout;
        this.buffer = ByteBuffer.allocateDirect(Math.max(4, initialVertices) * layout.stride())
                .order(ByteOrder.nativeOrder());
    }

    public VertexLayout getLayout() {
        return layout;
    }

    /**
     * Remove all vertices (capacity is kept).
     */
    public void clear() {
        buffer.clear();
        vertexCount = 0;
    }

    /**
     * @return The number of vertices written.
     */
    public int vertexCount() {
        return vertexCount;
    }

    /**
     * @return The number of quads written.
     */
    public int quadCount() {
        return vertexCount / 4;
    }

    /**
     * Make sure {@code vertices} more vertices fit without growing in between.
     *
     * @param vertices The number of additional vertices.
     */
    public void reserve(int vertices) {
        int needed = buffer.position() + vertices * layout.stride();
        if (needed > buffer.capacity()) {
            int capacity = buffer.capacity();
            while (capacity < needed) {
                capacity *= 2;
            }
            ByteBuffer grown = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
            buffer.flip();
            grown.put(buffer);
            buffer = grown;
        }
    }

    /**
     * Write one {@link VertexLayout#SPRITE} vertex. Call {@link #reserve(int)} first.
     *
     * @param x    Position x.
     * @param y    Position y.
     * @param u    Texture coordinate u.
     * @param v    Texture coordinate v.
     * @param rgba Packed color (R in the lowest byte).
     * @param slot Texture slot of the batch (0..255).
     */
    public void putSpriteVertex(float x, float y, float u, float v, int rgba, int slot) {
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(u);
        buffer.putFloat(v);
        buffer.putInt(LITTLE_ENDIAN ? rgba : Integer.reverseBytes(rgba));
        buffer.putInt(LITTLE_ENDIAN ? (slot & 0xFF) : (slot & 0xFF) << 24);
        vertexCount++;
    }

    /**
     * The underlying buffer, for backends that upload by address without creating views. Bytes
     * {@code [0, vertexCount * stride)} are valid; position and limit must not be changed.
     *
     * @return The internal buffer (shared, not a copy).
     */
    public ByteBuffer buffer() {
        return buffer;
    }

    /**
     * A read-only view of the written bytes {@code [fromVertex, toVertex)} for uploading. The view shares memory with
     * this stream and is only valid until the next write.
     *
     * @param fromVertex First vertex (inclusive).
     * @param toVertex   Last vertex (exclusive).
     * @return The byte view, positioned at 0.
     */
    public ByteBuffer slice(int fromVertex, int toVertex) {
        if (fromVertex < 0 || toVertex > vertexCount || fromVertex > toVertex) {
            throw new IndexOutOfBoundsException("Invalid vertex range [" + fromVertex + ", " + toVertex + ").");
        }
        int stride = layout.stride();
        return buffer.duplicate().order(ByteOrder.nativeOrder())
                .limit(toVertex * stride).position(fromVertex * stride).slice().order(ByteOrder.nativeOrder());
    }
}
