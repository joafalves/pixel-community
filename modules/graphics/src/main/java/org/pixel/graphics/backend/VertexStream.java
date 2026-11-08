package org.pixel.graphics.backend;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * CPU-side vertex data of one layer in a pass, ready to be uploaded by the backend.
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
        long needed = buffer.position() + (long) vertices * layout.stride();
        if (needed > buffer.capacity()) {
            long capacity = buffer.capacity();
            while (capacity < needed) {
                capacity *= 2;
            }
            if (capacity > Integer.MAX_VALUE) {
                throw new IllegalStateException("Vertex stream over 2 GB (" + needed + " bytes needed).");
            }
            ByteBuffer grown = ByteBuffer.allocateDirect((int) capacity).order(ByteOrder.nativeOrder());
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
        // (layout is checked by the batcher, which owns one stream per layout). Absolute puts and one position update
        // per vertex: measurably faster than relative puts, which check and move the position on every value.
        ByteBuffer b = buffer;
        int p = b.position();
        b.putFloat(p, x);
        b.putFloat(p + 4, y);
        b.putFloat(p + 8, u);
        b.putFloat(p + 12, v);
        b.putInt(p + 16, LITTLE_ENDIAN ? rgba : Integer.reverseBytes(rgba));
        b.putInt(p + 20, LITTLE_ENDIAN ? (slot & 0xFF) : (slot & 0xFF) << 24);
        b.position(p + 24);
        vertexCount++;
    }

    /**
     * Write one {@link VertexLayout#FULL} vertex. Call {@link #reserve(int)} first.
     *
     * @param x         Position x.
     * @param y         Position y.
     * @param u         Texture coordinate u (or shape dimension x).
     * @param v         Texture coordinate v (or shape dimension y).
     * @param rgba      Packed fill color (R in the lowest byte).
     * @param localX    Shape-local x.
     * @param localY    Shape-local y.
     * @param p0        Shape parameter 0.
     * @param p1        Shape parameter 1.
     * @param p2        Shape parameter 2.
     * @param p3        Shape parameter 3.
     * @param shapeType Shape type code (0..255).
     * @param slot      Texture slot (0..255).
     * @param clip      Batch-local clip index (0 = none).
     * @param style     Batch-local style index (0 = none).
     */
    public void putFullVertex(float x, float y, float u, float v, int rgba, float localX, float localY,
                              float p0, float p1, float p2, float p3, int shapeType, int slot, int clip, int style) {
        ByteBuffer b = buffer;
        int p = b.position();
        b.putFloat(p, x);
        b.putFloat(p + 4, y);
        b.putFloat(p + 8, u);
        b.putFloat(p + 12, v);
        b.putInt(p + 16, LITTLE_ENDIAN ? rgba : Integer.reverseBytes(rgba));
        b.putFloat(p + 20, localX);
        b.putFloat(p + 24, localY);
        b.putFloat(p + 28, p0);
        b.putFloat(p + 32, p1);
        b.putFloat(p + 36, p2);
        b.putFloat(p + 40, p3);
        b.put(p + 44, (byte) shapeType);
        b.put(p + 45, (byte) slot);
        b.putShort(p + 46, (short) clip);
        b.putShort(p + 48, (short) style);
        b.putShort(p + 50, (short) 0);
        b.position(p + 52);
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
