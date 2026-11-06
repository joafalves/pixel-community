package org.pixel.graphics;

/**
 * Per-frame rendering counters owned by the {@link GraphicsDevice}.
 * <p>
 * Renderers that talk to the GPU (today {@code GLSpriteBatch}, later the render backend) <b>record</b> into the
 * frame in progress; the device publishes it at the end of every frame ({@link #endFrame()}). Readers always see the
 * last <b>completed</b> frame through {@link #lastFrame()}, so values are stable for the whole frame and nobody has
 * to reset anything.
 * <p>
 * Counters are backend-level facts (what was submitted to the GPU), not per front-end: several front-ends can feed
 * the same draw call. Render thread only; not thread-safe.
 */
public final class RenderStats {

    private final Frame current = new Frame();
    private final Frame last = new Frame();
    private long frameNumber;

    /**
     * Record one GPU draw call.
     *
     * @param primitives The number of primitives drawn (sprites, glyphs, shapes).
     * @param vertices   The number of vertices processed.
     */
    public void recordDrawCall(long primitives, long vertices) {
        current.drawCalls++;
        current.primitives += primitives;
        current.vertices += vertices;
    }

    /**
     * Record a vertex/index data upload to the GPU.
     *
     * @param bytes The number of bytes uploaded.
     */
    public void recordUpload(long bytes) {
        current.uploadedBytes += bytes;
    }

    /**
     * Record a texture bind.
     */
    public void recordTextureBind() {
        current.textureBinds++;
    }

    /**
     * Publish the frame in progress as the last completed frame and start a new one. Called by the graphics device
     * once per frame; game code never needs to call it.
     */
    public void endFrame() {
        last.copyFrom(current);
        last.frameNumber = ++frameNumber;
        current.reset();
    }

    /**
     * Get the counters of the last completed frame. The returned object is reused and updated in place at the end of
     * every frame; copy the values if they must be kept.
     *
     * @return The last completed frame.
     */
    public Frame lastFrame() {
        return last;
    }

    /**
     * Counters of one frame (read-only view).
     */
    public static final class Frame {

        private long frameNumber;
        private long drawCalls;
        private long primitives;
        private long vertices;
        private long uploadedBytes;
        private long textureBinds;

        private Frame() {
        }

        private void reset() {
            drawCalls = 0;
            primitives = 0;
            vertices = 0;
            uploadedBytes = 0;
            textureBinds = 0;
        }

        private void copyFrom(Frame other) {
            drawCalls = other.drawCalls;
            primitives = other.primitives;
            vertices = other.vertices;
            uploadedBytes = other.uploadedBytes;
            textureBinds = other.textureBinds;
        }

        /**
         * @return The 1-based number of this frame (0 until the first frame completes).
         */
        public long getFrameNumber() {
            return frameNumber;
        }

        public long getDrawCalls() {
            return drawCalls;
        }

        public long getPrimitives() {
            return primitives;
        }

        public long getVertices() {
            return vertices;
        }

        public long getUploadedBytes() {
            return uploadedBytes;
        }

        public long getTextureBinds() {
            return textureBinds;
        }

        @Override
        public String toString() {
            return "Frame[" + frameNumber + ": drawCalls=" + drawCalls + ", primitives=" + primitives
                    + ", vertices=" + vertices + ", uploadedBytes=" + uploadedBytes
                    + ", textureBinds=" + textureBinds + "]";
        }
    }
}
